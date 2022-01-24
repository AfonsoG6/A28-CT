package com.example.app.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;
import com.example.app.exceptions.NotFoundInDatabaseException;
import com.example.hub.grpc.Hub;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class DatabaseHelper extends SQLiteOpenHelper {

	public DatabaseHelper(@Nullable Context context) {
		super(context, "sirs.db", null, 1);
	}


	@Override
	public void onCreate(SQLiteDatabase db) {
		String stmt = "CREATE TABLE IF NOT EXISTS sks (epoch_day INTEGER PRIMARY KEY NOT NULL, sk BLOB NOT NULL);";
		stmt += "CREATE TABLE IF NOT EXISTS recvd_msgs (interval_n INTEGER NOT NULL, msg BLOB NOT NULL, enc_lat BLOB, enc_long BLOB, PRIMARY KEY (interval_n, msg));";
		System.out.println("Creating SQlite Helper.");
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
	public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
		//TODO: Implement this, I have no idea what goes here
	}

	public void createTable() {
		SQLiteDatabase db = this.getWritableDatabase();
		String stmt = "CREATE TABLE IF NOT EXISTS recvd_msgs (interval_n INTEGER NOT NULL, msg BLOB NOT NULL, enc_lat BLOB, enc_long BLOB, PRIMARY KEY (interval_n, msg));";
		db.execSQL(stmt);
		db.close();
	}

	public void deleteSKsBefore(long epochDay) {
		SQLiteDatabase db = this.getWritableDatabase();
		String stmt = "DELETE FROM sks WHERE epoch_day < ?";
		String[] args = {String.valueOf(epochDay)};
		db.execSQL(stmt, args);
		db.close();
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
		SQLiteDatabase db = this.getReadableDatabase();

		String stmt = "SELECT sk FROM sks WHERE epoch_day = ?";
		String[] args = {String.valueOf(epochDay)};
		Cursor cursor = db.rawQuery(stmt, args);
		if (cursor.moveToFirst()) {
			byte[] sk = cursor.getBlob(0);
			cursor.close();
			return sk;
		}
		else {
			throw new NotFoundInDatabaseException();
		}
	}

	public boolean insertRecvdMessage(byte[] message, long intervalN, byte[] encLat, byte[] encLong) {
		SQLiteDatabase db = this.getWritableDatabase();
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
		long insert = db.insert("recvd_msgs", null, cv);
		db.close();
		return insert >= 0;
	}

	/* public boolean checkSKPresence(java.util.List<Hub.SKEpochDayPair> skList){
		try (SQLiteDatabase db = this.getReadableDatabase()) {
			System.out.println("Entered Database.");
			for(Hub.SKEpochDayPair pair : skList) {
				String stmt = "SELECT sk FROM sks WHERE epoch_day > ?";
				String[] args = {String.valueOf( -1)};
				try (Cursor cursor = db.rawQuery(stmt, args)) {
					System.out.println("Received SK:" + " " + new String(pair.getSk().toByteArray(), StandardCharsets.UTF_8));
					if (cursor.moveToFirst()) {
						byte[] sk = cursor.getBlob(0);
						System.out.println("I was able to receive from SQLite database: " + " " + new String(sk, StandardCharsets.UTF_8));
						if(Arrays.equals(sk,pair.getSk().toByteArray()))
							return true;
					}
				}
			}
			return false;
		}
	} */

	public ArrayList<receivedMsg> getAllSks(long epochDay) {
		ArrayList<receivedMsg> msgList = new ArrayList<>();
		try (SQLiteDatabase db = this.getReadableDatabase()) {
			System.out.println("Entered SQLITE Database.");
			String stmt = "SELECT interval_n, msg FROM recvd_msgs WHERE interval_n >= ?";
			String[] args = {String.valueOf(/*pair.getEpochDay() + 14*/  -1)}; //TODO change from two weeks to anything else (I don't remember what)
			try (Cursor c = db.rawQuery(stmt, args)) {
				if (c.moveToFirst()) {
					while (!c.isLast()) {
						receivedMsg msg = new receivedMsg(c.getLong(0), c.getBlob(1));
						msgList.add(msg);
						c.moveToNext();
					}
					receivedMsg msg = new receivedMsg(c.getLong(0), c.getBlob(1));
					msgList.add(msg);
					db.close();
					return msgList;
				}
			}
		}

		return msgList;
	}

	public class receivedMsg {

		private long interval_n;
		private byte[] msg;

		public receivedMsg(long interval_n, byte[] msg) {
			this.interval_n = interval_n;
			this.msg = msg;
		}

		public long getInterval_n() {
			return interval_n;
		}

		public byte[] getMsg() {
			return msg;
		}

	}


}
