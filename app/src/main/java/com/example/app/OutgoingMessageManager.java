package com.example.app;

import android.content.Context;
import com.example.app.database.DatabaseHelper;
import com.example.app.exceptions.DatabaseInsertionFailedException;
import com.example.app.exceptions.NotFoundInDatabaseException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Calendar;

public class OutgoingMessageManager {
	private static final int SECONDS_TO_UPDATE_MSG = 300; // 5min = 5*60s
	private static final int SECONDS_IN_DAY = 86400;
	private static final int SK_DELETED_AFTER_DAYS = 14;

	private byte[] currentSK;
	private long currentSKEpochDay;
	private byte[] currentMsg;
	private long currentMsgIntervalN;

	public OutgoingMessageManager(Context context) throws NoSuchAlgorithmException, DatabaseInsertionFailedException {
		updateCurrentSK(context);
	}

	private long getEpochTime() {
		return Calendar.getInstance().getTimeInMillis()/1000;
	}

	private long getEpochDay() {
		return getEpochTime()/SECONDS_IN_DAY;
	}

	private byte[] generateNewSK() throws NoSuchAlgorithmException {
		SecureRandom random = SecureRandom.getInstanceStrong();
		byte[] newSK = new byte[256];
		random.nextBytes(newSK);
		return newSK;
	}

	private byte[] generateNextSK(byte[] prevSK) throws NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		return digest.digest(prevSK);
	}

	public byte[] getSK(Context context, long epochday) throws NotFoundInDatabaseException {
		DatabaseHelper dbHelper = new DatabaseHelper(context);
		return dbHelper.getSK(epochday);
	}

	public void storeSK(Context context, long epochDay, byte[] sk) throws DatabaseInsertionFailedException {
		DatabaseHelper dbHelper = new DatabaseHelper(context);
		boolean success = dbHelper.insertSK(epochDay, sk);
		if (!success) {
			throw new DatabaseInsertionFailedException();
		}
	}

	public void cleanOldSKs(Context context) {
		long epochDay = getEpochDay();
		DatabaseHelper dbHelper = new DatabaseHelper(context);
		dbHelper.deleteSKsBefore(epochDay-SK_DELETED_AFTER_DAYS);
	}

	private void updateCurrentSK(Context context) throws NoSuchAlgorithmException, DatabaseInsertionFailedException {
		long epochdayToday = getEpochDay();
		cleanOldSKs(context);
		// Try to get TODAY's SK from database
		try {
			currentSK = getSK(context, epochdayToday);
			currentSKEpochDay = epochdayToday;
			return;
		}
		catch (NotFoundInDatabaseException ignored) { /* Just continue */ }
		// Try to get YESTERDAY's SK from database and generate a new one for today
		try {
			byte[] yesterdaySK = getSK(context, epochdayToday-1);
			currentSK = generateNextSK(yesterdaySK);
			currentSKEpochDay = epochdayToday;
			storeSK(context, epochdayToday, currentSK);
			return;
		}
		catch (NotFoundInDatabaseException | NoSuchAlgorithmException ignored) { /* Just continue */ }
		// Generate a new SK from scratch
		currentSK = generateNewSK();
		currentSKEpochDay = epochdayToday;
		storeSK(context, epochdayToday, currentSK);
	}

	private void updateCurrentMsg(Context context)
			throws NoSuchAlgorithmException, IOException, DatabaseInsertionFailedException {
		long epochTime = getEpochTime();
		long epochDay = getEpochDay();

		// Calculate at which interval of the day we're currently at and check if already using the correct Msg.
		long intervalN = (epochTime - epochDay)/SECONDS_TO_UPDATE_MSG;
		if (epochDay == currentSKEpochDay && intervalN == currentMsgIntervalN) return; // currentMsg is up-to-date

		if (epochDay != currentSKEpochDay) updateCurrentSK(context);

		byte[] intervalNBytes = ByteBuffer.allocate(8).putLong(intervalN).array();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
		outputStream.write(currentSK);
		outputStream.write(intervalNBytes);
		byte[] toHash = outputStream.toByteArray();

		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		currentMsg = digest.digest(toHash);
		currentMsgIntervalN = intervalN;
	}

}
