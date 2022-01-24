package com.example.app.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;
import com.example.app.Constants;
import com.example.app.SecureStorageManager;
import com.example.app.exceptions.DecryptionFailedException;
import com.example.app.exceptions.NotFoundInDatabaseException;
import com.example.app.exceptions.PasswordCheckFailedException;
import com.example.hub.grpc.Hub;
import com.google.protobuf.ByteString;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class DatabaseHelper extends SQLiteOpenHelper {
	private static final String TAG = DatabaseHelper.class.getName();

	private final Context context;

	public DatabaseHelper(@Nullable Context context) {
		super(context, "sirs.db", null, 1);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String stmt = "CREATE TABLE IF NOT EXISTS sks (epoch_day INTEGER PRIMARY KEY NOT NULL, sk BLOB NOT NULL);";
		db.execSQL(stmt);
		stmt += "CREATE TABLE IF NOT EXISTS recvd_msgs (intervalN INTEGER NOT NULL, msg BLOB NOT NULL, enc_lat BLOB, enc_long BLOB, infected INTEGER NOT NULL, PRIMARY KEY (intervalN, msg));";
		db.execSQL(stmt);
	}

	/* Don't call this inside onCreate! Or anywhere else where a database is already opened! */
	public void populateSKs() {
		Random random = new Random(2336);
		for (int i=0; i<14; i++) {
			byte[] sk = new byte[256];
			random.nextBytes(sk);
			insertSK(i, sk);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) { /* Empty */ }

	public void deleteOldSKs() {
		long epochDay = EpochHelper.getCurrentEpochDay() - Constants.SK_DELETED_AFTER_DAYS;
		try (SQLiteDatabase db = this.getWritableDatabase()) {
			String stmt = "DELETE FROM sks WHERE epoch_day < ?";
			String[] args = {String.valueOf(epochDay)};
			db.execSQL(stmt, args);
		}
	}

	public boolean insertSK(long epochDay, byte[] sk) {
		try (SQLiteDatabase db = this.getWritableDatabase()) {
			ContentValues cv = new ContentValues();
			cv.put("epoch_day", epochDay);
			cv.put("sk", sk);

			long insert = db.insert("sks", null, cv);
			return insert >= 0;
		}
	}

	public byte[] getSK(long epochDay) throws NotFoundInDatabaseException {
		try (SQLiteDatabase db = this.getReadableDatabase()) {
			String stmt = "SELECT sk FROM sks WHERE epoch_day = ?";
			String[] args = {String.valueOf(epochDay)};
			try (Cursor cursor = db.rawQuery(stmt, args)) {
				if (cursor.moveToFirst()) {
					return cursor.getBlob(0);
				}
				else {
					throw new NotFoundInDatabaseException();
				}
			}
		}
	}

	public List<Hub.SKEpochDayPair> getAllSKs() {
		deleteOldSKs();
		List<Hub.SKEpochDayPair> skEpochDayPairs = new ArrayList<>();
		try (SQLiteDatabase db = this.getReadableDatabase()) {
			String stmt = "SELECT epoch_day, sk FROM sks";
			try (Cursor cursor = db.rawQuery(stmt, null)) {
				if (cursor.moveToFirst()) {
					do {
						Hub.SKEpochDayPair skEpochDayPair = Hub.SKEpochDayPair.newBuilder()
								.setEpochDay(cursor.getInt(0))
								.setSk(ByteString.copyFrom(cursor.getBlob(1)))
								.build();
					} while (cursor.moveToNext());
				}
			}
		}
		return skEpochDayPairs;
	}

	public void deleteOldRecvdMsgs() {
		long intervalN = EpochHelper.getCurrentInterval() - Constants.MSG_DELETED_AFTER_INTERVALS;
		try (SQLiteDatabase db = this.getWritableDatabase()) {
			String stmt = "DELETE FROM recvd_msgs WHERE interval_n < ?";
			String[] args = {String.valueOf(intervalN)};
			db.execSQL(stmt, args);
		}
	}

	public boolean insertRecvdMessage(byte[] message, long intervalN, byte[] encLat, byte[] encLong) {
		try (SQLiteDatabase db = this.getWritableDatabase()) {
			ContentValues cv = new ContentValues();
			cv.put("msg", message);
			cv.put("interval_n", intervalN);
			if (encLat.length == 0 || encLong.length == 0) {
				// If failed to get full location information, don't insert any location information
				cv.putNull("enc_lat");
				cv.putNull("enc_long");
			}
			else {
				cv.put("enc_lat", encLat);
				cv.put("enc_long", encLong);
			}
			cv.put("infected", 0);
			long insert = db.insert("recvd_msgs", null, cv);
			return insert >= 0;
		}
	}

	public boolean existsRecvdMessage(byte[] message, long intervalN) {
		try (SQLiteDatabase db = this.getReadableDatabase()) {
			String stmt = "SELECT * FROM recvd_msgs WHERE interval_n = ? AND msg = ?";
			String[] args = {String.valueOf(intervalN), Arrays.toString(message)};
			try (Cursor cursor = db.rawQuery(stmt, args)) {
				return cursor.moveToFirst();
			}
		}
	}

	public int getNumContacts() {
		try (SQLiteDatabase db = this.getReadableDatabase()) {
			String stmt = "SELECT COUNT(*) FROM recvd_msgs WHERE infected = 1";
			try (Cursor cursor = db.rawQuery(stmt, null)) {
				if (cursor.moveToFirst())
					return cursor.getInt(0);
				else
					return 0;
			}
		}
	}

	public void updateContact(byte[] currentMsg, long intervalN) {
		try (SQLiteDatabase db = this.getWritableDatabase()) {
			String stmt = "UPDATE recvd_msgs SET infected = 1 WHERE interval_n = ? AND msg = ?";
			String[] args = {String.valueOf(intervalN), Arrays.toString(currentMsg)};
			db.execSQL(stmt, args);
		}
	}

	public List<ContactInfo> getInfectedContacts(String password)
			throws PasswordCheckFailedException, NoSuchAlgorithmException {
		SecureStorageManager ssManager = new SecureStorageManager(context);
		byte[] privateKey = ssManager.getDeobfuscatedPrivateKey(password);
		List<ContactInfo> contactInfos = new ArrayList<>();

		try (SQLiteDatabase db = this.getReadableDatabase()) {
			String stmt = "SELECT interval_n, enc_lat, enc_long FROM recvd_msgs WHERE infected = 1";
			try (Cursor cursor = db.rawQuery(stmt, null)) {
				if (cursor.moveToFirst()) {
					do {
						long intervalN = cursor.getLong(0);
						boolean validLocation = true;
						double latitude = 0;
						double longitude = 0;
						try {
							latitude = ssManager.decryptValue(privateKey, cursor.getBlob(1));
							longitude = ssManager.decryptValue(privateKey, cursor.getBlob(2));
						} catch (DecryptionFailedException e) {
							e.printStackTrace();
							validLocation = false;
						}
						contactInfos.add(new ContactInfo(intervalN, validLocation, latitude, longitude));
					} while (cursor.moveToNext());
				}
				return contactInfos;
			}
		}
	}

	public static class ContactInfo {
		public final long intervalN;
		public final boolean validLocation;
		public final double latitude;
		public final double longitude;

		public ContactInfo(long intervalN, boolean validLocation, double latitude, double longitude) {
			this.intervalN = intervalN;
			this.validLocation = validLocation;
			this.latitude = latitude;
			this.longitude = longitude;
		}
	}
}
