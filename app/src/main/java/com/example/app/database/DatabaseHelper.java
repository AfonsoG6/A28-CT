package com.example.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;
import com.example.app.exceptions.NotFoundInDatabaseException;

public class DatabaseHelper extends SQLiteOpenHelper {
	public static final String TABLE_SKS = "sks";
	public static final String COLUMN_EPOCH_DAY = "epoch_day";
	public static final String COLUMN_SK = "sk";

	public DatabaseHelper(@Nullable Context context) {
		super(context, "sirs.db", null, 1);
	}


	@Override
	public void onCreate(SQLiteDatabase sqLiteDatabase) {
		String stmt = "CREATE TABLE " + TABLE_SKS + " (" + COLUMN_EPOCH_DAY + " INTEGER PRIMARY KEY, " + COLUMN_SK + " BLOB NOT NULL);";
		// TODO Add Creation of Location Table too
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL(stmt);
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

	}

	public void deleteSKsBefore(long epochDay) {
		SQLiteDatabase db = this.getWritableDatabase();
		String stmt = "DELETE FROM " + TABLE_SKS + " WHERE " + COLUMN_EPOCH_DAY + " < ?";
		String[] args = {String.valueOf(epochDay)};
		db.execSQL(stmt, args);
	}

	public boolean insertSK(long epochDay, byte[] sk) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_EPOCH_DAY, epochDay);
		cv.put(COLUMN_SK, sk);

		long insert = db.insert(TABLE_SKS, null, cv);
		return insert >= 0;
	}

	public byte[] getSK(long epochDay) throws NotFoundInDatabaseException {
		SQLiteDatabase db = this.getReadableDatabase();

		String stmt = "SELECT " + COLUMN_SK + " FROM " + TABLE_SKS + " WHERE " + COLUMN_EPOCH_DAY + " = ?";
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
}
