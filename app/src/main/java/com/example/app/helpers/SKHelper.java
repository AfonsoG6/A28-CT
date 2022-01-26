package com.example.app.helpers;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class SKHelper {
	private static final String TAG = SKHelper.class.getName();

	private SKHelper() { /* Empty */ }

	public static byte[] generateMsg(byte[] sk, long intervalN) throws IOException, NoSuchAlgorithmException {
		byte[] intervalNBytes = ByteBuffer.allocate(8).putLong(intervalN).array();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
		outputStream.write(sk);
		outputStream.write(intervalNBytes);
		byte[] toHash = outputStream.toByteArray();

		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		byte[] msg = digest.digest(toHash);
		Log.d(TAG, "Generated Msg from (SK, intervalN): (" + Arrays.toString(sk) + ", " + intervalN + ") -> " + Arrays.toString(msg) + "( " + msg.length + " )");
		return msg;
	}
}
