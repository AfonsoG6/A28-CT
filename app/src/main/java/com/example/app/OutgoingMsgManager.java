package com.example.app;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import com.example.app.bluetooth.BleMessage;
import com.example.app.bluetooth.BleScanner;
import com.example.app.bluetooth.ContactServer;
import com.example.app.exceptions.DatabaseInsertionFailedException;
import com.example.app.exceptions.NotFoundInDatabaseException;
import com.example.app.helpers.DatabaseHelper;
import com.example.app.helpers.EpochHelper;
import com.example.app.helpers.SKHelper;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class OutgoingMsgManager {

	private byte[] currentSK;
	private long currentSKEpochDay;
	private byte[] currentMsg;
	private long currentMsgIntervalN;

	public OutgoingMsgManager(Context context)
			throws NoSuchAlgorithmException, DatabaseInsertionFailedException, IOException {
		updateCurrentSK(context);
		updateCurrentMsg(context);
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
		try (DatabaseHelper dbHelper = new DatabaseHelper(context)) {
			dbHelper.deleteOldSKs();
		}
	}

	private void updateCurrentSK(Context context) throws NoSuchAlgorithmException, DatabaseInsertionFailedException {
		long epochdayToday = EpochHelper.getCurrentEpochDay();
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
		long epochDay = EpochHelper.getCurrentEpochDay();

		// Calculate the time interval that we're currently at and check if already using the correct Msg.
		long intervalN = EpochHelper.getCurrentInterval();

		if (epochDay == currentSKEpochDay && intervalN == currentMsgIntervalN) return; // currentMsg is up-to-date

		if (epochDay != currentSKEpochDay) updateCurrentSK(context);

		currentMsg = SKHelper.generateMsg(currentSK, intervalN);
		currentMsgIntervalN = intervalN;
	}

	public void sendContactMsg(Context context)
			throws DatabaseInsertionFailedException, NoSuchAlgorithmException, IOException {
		updateCurrentMsg(context);
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
