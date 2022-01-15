package com.example.app;

import com.example.app.exceptions.NotFoundInDatabaseException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Calendar;

public class ContactMessageManager {
	private static final int SECONDS_TO_UPDATE_MSG = 300; // 5min = 5*60s
	private static final int SECONDS_IN_DAY = 86400;

	private byte[] currentSK;
	private byte[] currentMsg;
	private long currentMsgIntervalN;

	public ContactMessageManager() throws NoSuchAlgorithmException {
		updateCurrentSK();
	}

	private long getEpochTime() {
		return Calendar.getInstance().getTimeInMillis()/1000;
	}


	private long getEpochDay() {
		return getEpochTime()/SECONDS_IN_DAY;
	}

	private void updateCurrentSK() throws NoSuchAlgorithmException {
		long epochdayToday = getEpochDay();
		byte[] sk;

		try {   // Try to get TODAY's SK from database
			sk = getSK(epochdayToday);
			currentSK = sk;
			return;
		}
		catch (NotFoundInDatabaseException ignored) { /* Just continue */ }

		try {   // Try to get YESTERDAY's SK from database and generate a new one for today
			sk = getSK(epochdayToday-1);
			currentSK = generateNewSK(sk);
			return;
		}
		catch (NotFoundInDatabaseException | NoSuchAlgorithmException ignored) { /* Just continue */ }

		// Generate a new SK from scratch
		currentSK = generateNewSK();
	}

	private void updateCurrentMsg() throws NoSuchAlgorithmException, IOException {
		long epochTime = getEpochTime();
		long epochDay = getEpochDay();

		// Calculate at which interval of the day we're currently at and check if already using the correct Msg.
		long intervalN = (epochTime - epochDay)/SECONDS_TO_UPDATE_MSG;
		if (intervalN == currentMsgIntervalN) return;

		byte[] intervalNBytes = ByteBuffer.allocate(8).putLong(intervalN).array();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
		outputStream.write(currentSK);
		outputStream.write(intervalNBytes);
		byte[] toHash = outputStream.toByteArray();

		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		currentMsg = digest.digest(toHash);
		currentMsgIntervalN = intervalN;
	}

	private byte[] generateNewSK() throws NoSuchAlgorithmException {
		SecureRandom random = SecureRandom.getInstanceStrong();
		byte[] newSK = new byte[256];
		random.nextBytes(newSK);
		return newSK;
	}

	private byte[] generateNewSK(byte[] prevSK) throws NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		return digest.digest(prevSK);
	}

	public byte[] getSK(long epochday) throws NotFoundInDatabaseException {
		//TODO: Access database to search for SK associated with epochday, throw exception if not found.
		byte[] foundSK = new byte[256];
		return foundSK;
	}
}
