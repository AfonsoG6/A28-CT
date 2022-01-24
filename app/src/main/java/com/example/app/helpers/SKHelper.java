package com.example.app.helpers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SKHelper {
	private SKHelper() { /* Empty */ }

	public static byte[] generateMsg(byte[] sk, long intervalN) throws IOException, NoSuchAlgorithmException {
		byte[] intervalNBytes = ByteBuffer.allocate(8).putLong(intervalN).array();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
		outputStream.write(sk);
		outputStream.write(intervalNBytes);
		byte[] toHash = outputStream.toByteArray();

		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		return digest.digest(toHash);
	}
}
