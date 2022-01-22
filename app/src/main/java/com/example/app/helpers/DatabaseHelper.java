package com.example.app.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;
import com.example.app.exceptions.NotFoundInDatabaseException;

public class DatabaseHelper extends SQLiteOpenHelper {

	public DatabaseHelper(@Nullable Context context) {
		super(context, "sirs.db", null, 1);
	}


	@Override
	public void onCreate(SQLiteDatabase db) {
		String stmt = "CREATE TABLE IF NOT EXISTS sks (epoch_day INTEGER PRIMARY KEY NOT NULL, sk BLOB NOT NULL);";
		db.execSQL(stmt);
		stmt += "CREATE TABLE IF NOT EXISTS recvd_msgs (interval_n INTEGER NOT NULL, msg BLOB NOT NULL, enc_lat BLOB, enc_long BLOB, PRIMARY KEY (interval_n, msg));";
		db.execSQL(stmt);
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
		//TODO: Implement this, I have no idea what goes here
	}

	public void deleteSKsBefore(long epochDay) {
		SQLiteDatabase db = this.getWritableDatabase();
		String stmt = "DELETE FROM sks WHERE epoch_day < ?";
		String[] args = {String.valueOf(epochDay)};
		db.execSQL(stmt, args);
	}

	public boolean insertSK(long epochDay, byte[] sk) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put("epoch_day", epochDay);
		cv.put("sk", sk);

		long insert = db.insert("sks", null, cv);
		return insert >= 0;
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
		return insert >= 0;
	}
}