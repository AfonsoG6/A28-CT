package com.example.app;

import android.content.Context;
import com.example.app.exceptions.PasswordSetFailedException;
import com.example.app.helpers.SharedPrefsHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.*;
import java.util.Arrays;

public class SecureStorageManager {
	public static final String TAG = "SecureStorageManager";
	public static final int SALT_LENGTH = 16;
	public static final int KEY_LENGTH = 2048;

	private SecureRandom secureRandom;
	private SharedPrefsHelper spHelper;

	public SecureStorageManager(Context context) throws PasswordSetFailedException {
		try {
			secureRandom = SecureRandom.getInstanceStrong();
			spHelper = new SharedPrefsHelper(context);
		}
		catch (NoSuchAlgorithmException e) {
			throw new PasswordSetFailedException(e);
		}
	}

	public void setPassword(String password) throws PasswordSetFailedException {
		if (spHelper.hasPasswordSet()) return;
		try {
			byte[] chkSalt = genSalt();
			byte[] obfSalt = genSalt();
			byte[] chkHash = hash(password, chkSalt);
			genAndSetKeyPair(hash(password, obfSalt));
			spHelper.setCheckSalt(chkSalt);
			spHelper.setObfuscationSalt(obfSalt);
			spHelper.setCheckHash(chkHash);
		}
		catch (NoSuchAlgorithmException | IOException e) {
			throw new PasswordSetFailedException(e);
		}
	}

	private void genAndSetKeyPair(byte[] obfHash)
			throws NoSuchAlgorithmException, PasswordSetFailedException, IOException {
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		keyGen.initialize(KEY_LENGTH, secureRandom);
		KeyPair keyPair = keyGen.generateKeyPair();

		byte[] privateKey = keyPair.getPrivate().getEncoded();
		byte[] extendedObfHash = extendHash(obfHash, privateKey.length);
		byte[] obfPrivKey = obfuscate(privateKey, extendedObfHash);
		byte[] publicKey = keyPair.getPublic().getEncoded();

		spHelper.setObfuscatedPrivateKey(obfPrivKey);
		spHelper.setPublicKey(publicKey);
	}

	private byte[] extendHash(byte[] obfHash, int length) throws IOException, NoSuchAlgorithmException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		os.write(obfHash);
		byte[] extension = obfHash;
		for (int i=0; i<(length/obfHash.length)-1; i++) {
			extension = hash(extension);
			os.write(extension);
		}
		// Add the remaining amount of bytes
		extension = Arrays.copyOfRange(hash(extension), 0, length%obfHash.length);
		os.write(extension);
		return os.toByteArray();
	}

	private byte[] obfuscate(byte[] privateKey, byte[] obfHash) throws PasswordSetFailedException {
		if (obfHash.length != privateKey.length) throw new PasswordSetFailedException("Invalid Obfuscation Hash length (Expected "+privateKey.length+", got " + obfHash.length + ")");
		byte[] result = new byte[privateKey.length];
		for (int i=0; i<privateKey.length; i++) {
			result[i] = (byte) (privateKey[i] ^ obfHash[i]);
		}
		return result;
	}

	private byte[] genSalt() {
		byte[] salt = new byte[SALT_LENGTH];
		secureRandom.nextBytes(salt);
		return salt;
	}

	private byte[] hash(String password, byte[] salt) throws IOException, NoSuchAlgorithmException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		os.write(password.getBytes());
		os.write(salt);
		byte[] toHash = os.toByteArray();

		MessageDigest digest = MessageDigest.getInstance("SHA-512");
		return digest.digest(toHash);
	}

	private byte[] hash(byte[] data) throws NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance("SHA-512");
		return digest.digest(data);
	}


}
