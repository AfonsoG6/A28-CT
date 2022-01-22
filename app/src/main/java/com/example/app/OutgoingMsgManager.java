package com.example.app;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import com.example.app.bluetooth.BleMessage;
import com.example.app.bluetooth.BleScanner;
import com.example.app.bluetooth.ContactServer;
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

public class OutgoingMsgManager {
	private static final int SECONDS_TO_UPDATE_MSG = 300; // 5min = 5*60s
	private static final int SECONDS_IN_DAY = 86400;
	private static final int SK_DELETED_AFTER_DAYS = 14;

	private byte[] currentSK;
	private long currentSKEpochDay;
	private byte[] currentMsg;
	private long currentMsgIntervalN;

	public OutgoingMsgManager(Context context) throws NoSuchAlgorithmException, DatabaseInsertionFailedException {
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
		byte[] newSK = new byte[32]; // 256 bits = 32 bytes
		random.nextBytes(newSK);
		return newSK;
	}

	private byte[] generateNextSK(byte[] prevSK) throws NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		return digest.digest(prevSK);
	}

	public byte[] getSK(Context context, long epochday) throws NotFoundInDatabaseException {
		try (DatabaseHelper dbHelper = new DatabaseHelper(context)) {
			return dbHelper.getSK(epochday);
		}
	}

	public void storeSK(Context context, long epochDay, byte[] sk) throws DatabaseInsertionFailedException {
		boolean success;
		try (DatabaseHelper dbHelper = new DatabaseHelper(context)) {
			success = dbHelper.insertSK(epochDay, sk);
		}
		if (!success) throw new DatabaseInsertionFailedException();
	}

	public void cleanOldSKs(Context context) {
		long epochDay = getEpochDay();
		try (DatabaseHelper dbHelper = new DatabaseHelper(context)) {
			dbHelper.deleteSKsBefore(epochDay - SK_DELETED_AFTER_DAYS);
		}
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

		// Calculate the time interval that we're currently at and check if already using the correct Msg.
		long intervalN = epochTime/SECONDS_TO_UPDATE_MSG;
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

	public void sendContactMsg(Context context)
			throws DatabaseInsertionFailedException, NoSuchAlgorithmException, IOException {
		updateCurrentMsg(context);
		//TODO: Use Bluetooth LE to send currentMsg and ?currentMsgIntervalN?
		byte[] message = new BleMessage(this.currentMsg, this.currentMsgIntervalN).toByteArray();
		new Thread() {
			@Override
			public void run() {
				super.run();
				try {
					BluetoothAdapter adapter = ((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE))
							.getAdapter();
					BleScanner scanner = new BleScanner(adapter);
					scanner.startScan();
					sleep(BleScanner.SCAN_PERIOD);
					scanner.stopScan();
					ContactServer.connectDevices(context, scanner.getScanResults());
					sleep(1000);
					ContactServer.sendMessage(message);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};
		ContactServer.connectDevices(context, null);
	}

}
