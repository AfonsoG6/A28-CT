package com.example.app;

import android.content.Context;
import android.util.Log;
import com.example.app.exceptions.DecryptionFailedException;
import com.example.app.exceptions.PasswordCheckFailedException;
import com.example.app.exceptions.PasswordSetFailedException;
import com.example.app.helpers.SharedPrefsHelper;
import org.conscrypt.Conscrypt;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

public class SecureStorageManager {
	public static final String TAG = SecureStorageManager.class.getName();
	public static final int SALT_LENGTH = 16;
	public static final int KEY_LENGTH = 2048;

	private SecureRandom secureRandom;
	private SharedPrefsHelper spHelper;

	public SecureStorageManager(Context context) throws NoSuchAlgorithmException {
		Security.insertProviderAt(Conscrypt.newProvider(), Security.getProviders().length);
		secureRandom = SecureRandom.getInstanceStrong();
		spHelper = new SharedPrefsHelper(context);
	}

	public void setPassword(String password) throws PasswordSetFailedException {
		if (spHelper.hasPasswordSet()) return;
		try {
			byte[] chkSalt = genSalt();
			Log.d(TAG, "Generated Check Salt: " + Arrays.toString(chkSalt));
			byte[] obfSalt = genSalt();
			Log.d(TAG, "Generated Obfuscation Salt: " + Arrays.toString(obfSalt));
			byte[] chkHash = hash(password, chkSalt);
			Log.d(TAG, "Calculated Check Hash: " + Arrays.toString(chkHash));
			genAndSetKeyPair(hash(password, obfSalt));
			spHelper.setCheckSalt(chkSalt);
			spHelper.setObfuscationSalt(obfSalt);
			spHelper.setCheckHash(chkHash);
		} catch (NoSuchAlgorithmException | IOException e) {
			throw new PasswordSetFailedException(e);
		}
	}

	public byte[] getDeobfuscatedPrivateKey(String password) throws PasswordCheckFailedException {
		try {
			if (!passwordMatches(password)) throw new PasswordCheckFailedException();

			byte[] obfPrivKey = spHelper.getObfuscatedPrivateKey();
			byte[] obfHash = hash(password, spHelper.getObfuscationSalt());
			byte[] res = deobfuscate(obfPrivKey, obfHash);
			Log.d(TAG, "Deobfuscated Private Key: " + Arrays.toString(res));
			return res;
		}
		catch (IOException | NoSuchAlgorithmException e) {
			Log.e(TAG, "Failed to check password", e);
			throw new PasswordCheckFailedException(e);
		}
	}

	public boolean passwordMatches(String passwordAttempt) throws PasswordCheckFailedException {
		try {
			byte[] hashedPassword = spHelper.getCheckHash();
			byte[] salt = spHelper.getCheckSalt();
			byte[] hashedPasswordAttempt = hash(passwordAttempt, salt);
			Log.d(TAG, "Comparing: " + Arrays.toString(hashedPasswordAttempt) + " with " + Arrays.toString(hashedPassword));
			return Arrays.equals(hashedPassword, hashedPasswordAttempt);
		} catch (IOException | NoSuchAlgorithmException e) {
			Log.e(TAG, "Failed to check password", e);
			throw new PasswordCheckFailedException(e);
		}
	}

	private void genAndSetKeyPair(byte[] obfHash)
			throws NoSuchAlgorithmException, PasswordSetFailedException, IOException {
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		keyGen.initialize(KEY_LENGTH, secureRandom);
		KeyPair keyPair = keyGen.generateKeyPair();

		byte[] publicKey = keyPair.getPublic().getEncoded();
		Log.d(TAG, "Generated Public Key: " + Arrays.toString(publicKey));
		byte[] privateKey = keyPair.getPrivate().getEncoded();
		Log.d(TAG, "Generated Private Key: " + Arrays.toString(privateKey));
		byte[] obfPrivKey = obfuscate(privateKey, obfHash);
		Log.d(TAG, "Calculated Obfuscated Private Key: " + Arrays.toString(obfPrivKey));

		spHelper.setObfuscatedPrivateKey(obfPrivKey);
		spHelper.setPublicKey(publicKey);
	}

	private byte[] extendHash(byte[] obfHash, int length) throws IOException, NoSuchAlgorithmException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		os.write(obfHash);
		byte[] extension = obfHash;
		for (int i = 0; i < (length / obfHash.length) - 1; i++) {
			extension = hash(extension);
			os.write(extension);
		}
		// Add the remaining amount of bytes
		extension = Arrays.copyOfRange(hash(extension), 0, length % obfHash.length);
		os.write(extension);
		return os.toByteArray();
	}

	private byte[] xorByteArrays(byte[] a, byte[] b) {
		byte[] result = new byte[a.length];
		for (int i = 0; i < a.length; i++) {
			result[i] = (byte) (a[i] ^ b[i]);
		}
		return result;
	}

	private byte[] obfuscate(byte[] privateKey, byte[] obfHash) throws PasswordSetFailedException {
		try {
			byte[] extendedObfHash = extendHash(obfHash, privateKey.length);
			if (extendedObfHash.length != privateKey.length) {
				Log.e(TAG, "Invalid Obfuscation Hash length (Expected " + privateKey.length + ", got " + extendedObfHash.length + ")");
				throw new PasswordSetFailedException("Invalid Obfuscation Hash length (Expected " + privateKey.length + ", got " + extendedObfHash.length + ")");
			}
			return xorByteArrays(privateKey, extendedObfHash);
		} catch (IOException | NoSuchAlgorithmException e) {
			throw new PasswordSetFailedException(e);
		}
	}

	private byte[] deobfuscate(byte[] privateKey, byte[] obfHash) throws IOException, NoSuchAlgorithmException {
		byte[] extendedObfHash = extendHash(obfHash, privateKey.length);
		if (obfHash.length != privateKey.length) {
			Log.e(TAG, "Invalid Obfuscation Hash length (Expected " + privateKey.length + ", got " + obfHash.length + ")");
		}
		return xorByteArrays(privateKey, extendedObfHash);
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

	public byte[] encryptValue(double value) {
		try {
			byte[] publicKeyBytes = spHelper.getPublicKey();
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publicKeyBytes));
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			byte[] valueBytes = ByteBuffer.allocate(8).putDouble(value).array();
			byte[] res = cipher.doFinal(valueBytes);
			Log.d(TAG, "Encrypted value: " + value + " -> " + Arrays.toString(res));
			return res;
		} catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | InvalidKeySpecException | BadPaddingException | InvalidKeyException e) {
			// If failed to encrypt, return an empty byte array
			e.printStackTrace();
			Log.e(TAG, "Failed to encrypt value: " + value + " (" + e.getMessage() + ")");
			return new byte[0];
		}
	}

	public double decryptValue(byte[] privateKey, byte[] value) throws DecryptionFailedException {
		try {
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			PrivateKey privateKeyObject = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(privateKey));
			cipher.init(Cipher.DECRYPT_MODE, privateKeyObject);
			byte[] decryptedValue = cipher.doFinal(value);
			double res = ByteBuffer.wrap(decryptedValue).getDouble();
			Log.d(TAG, "Decrypted value: " + Arrays.toString(value) + " -> " + res);
			return res;
		} catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | InvalidKeySpecException | BadPaddingException | InvalidKeyException e) {
			Log.e(TAG, "Failed to decrypt value: " + Arrays.toString(value) + " (" + e.getMessage() + ")");
			throw new DecryptionFailedException(e);
		}
	}

}
