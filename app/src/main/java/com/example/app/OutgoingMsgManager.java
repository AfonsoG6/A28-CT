package com.example.app;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.util.Log;
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
import java.util.Arrays;

public class OutgoingMsgManager {
	private static final String TAG = OutgoingMsgManager.class.getName();

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
		Log.d(TAG, "Generated new SK: " + Arrays.toString(newSK) + " (" + newSK.length + ")");
		return newSK;
	}

	private byte[] generateNextSK(byte[] prevSK) throws NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		byte[] nextSK = digest.digest(prevSK);
		Log.d(TAG, "Generated next SK from previous SK: " + Arrays.toString(prevSK) + " (" + prevSK.length + ")  -> " + Arrays.toString(nextSK) + " (" + nextSK.length + ")");
		return nextSK;
	}

	public byte[] getSK(Context context, long epochday) throws NotFoundInDatabaseException {
		try (DatabaseHelper dbHelper = new DatabaseHelper(context)) {
			byte[] sk = dbHelper.getSK(epochday);
			Log.d(TAG, "Retrieved SK for epochday " + epochday + ": " + Arrays.toString(sk) + " (" + sk.length + ")");
			return sk;
		}
	}

	public void storeSK(Context context, long epochDay, byte[] sk) throws DatabaseInsertionFailedException {
		boolean success;
		Log.d(TAG, "Storing SK for epochday " + epochDay + ": " + Arrays.toString(sk) + " (" + sk.length + ")");
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
		Log.d(TAG, "Updating current SK for today (epochDay = " + epochdayToday + ")");
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
		Log.d(TAG, "Updated current Msg for intervalN " + intervalN + ": " + Arrays.toString(currentMsg) + " (" + currentMsg.length + ")");
	}

	public void sendContactMsg(Context context)
			throws DatabaseInsertionFailedException, NoSuchAlgorithmException, IOException {
		updateCurrentMsg(context);
		Log.d(TAG, "Sending contact Msg for intervalN " + currentMsgIntervalN + ": " + Arrays.toString(currentMsg) + " (" + currentMsg.length + ")");
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
					sleep(Constants.BLE_SCAN_TIME);
					scanner.stopScan();
					ContactServer.connectDevices(context, scanner.getScanResults());
					sleep(Constants.BLS_CONNECTION_TIME);
					ContactServer.sendMessage(message);
				} catch (InterruptedException e) {
					e.printStackTrace();
					Thread.currentThread().interrupt();
				}
			}
		}.start();
	}

}
