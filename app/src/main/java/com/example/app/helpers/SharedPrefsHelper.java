package com.example.app.helpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Arrays;
import java.util.Base64;

public class SharedPrefsHelper {
	private static final String TAG = SharedPrefsHelper.class.getName();

	private SharedPreferences preferences;

	public SharedPrefsHelper(Context context) {
		preferences = context.getSharedPreferences("com.example.app", Context.MODE_PRIVATE);
	}

	public boolean hasPasswordSet() {
		return preferences.contains("check_hash");
	}
	//--------------------------------------------------Check Salt------------------------------------------------------
	@SuppressLint("ApplySharedPref")
	public void setCheckSalt(byte[] salt) {
		Log.d(TAG, "Setting check salt: " + Arrays.toString(salt));
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString("check_salt", Base64.getEncoder().encodeToString(salt));
		editor.commit();
	}

	public byte[] getCheckSalt() {
		byte[] chkSalt = Base64.getDecoder().decode(preferences.getString("check_salt", ""));
		Log.d(TAG, "Got check salt: " + Arrays.toString(chkSalt) + "(length: " + chkSalt.length + ")");
		return chkSalt;
	}
	//-----------------------------------------------Obfuscation Salt---------------------------------------------------
	@SuppressLint("ApplySharedPref")
	public void setObfuscationSalt(byte[] salt) {
		Log.d(TAG, "Setting obfuscation salt: " + Arrays.toString(salt));
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString("obfuscation_salt", Base64.getEncoder().encodeToString(salt));
		editor.commit();
	}

	public byte[] getObfuscationSalt() {
		byte[] obfSalt = Base64.getDecoder().decode(preferences.getString("obfuscation_salt", ""));
		Log.d(TAG, "Got obfuscation salt: " + Arrays.toString(obfSalt) + "(length: " + obfSalt.length + ")");
		return obfSalt;
	}
	//--------------------------------------------------Check Hash------------------------------------------------------
	@SuppressLint("ApplySharedPref")
	public void setCheckHash(byte[] hashedPassword) {
		Log.d(TAG, "Setting check hash: " + Arrays.toString(hashedPassword));
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString("check_hash", Base64.getEncoder().encodeToString(hashedPassword));
		editor.commit();
	}

	public byte[] getCheckHash() {
		byte[] chkHash = Base64.getDecoder().decode(preferences.getString("check_hash", ""));
		Log.d(TAG, "Got check hash: " + Arrays.toString(chkHash) + "(length: " + chkHash.length + ")");
		return chkHash;
	}

	//-------------------------------------------------Public Key-------------------------------------------------------
	@SuppressLint("ApplySharedPref")
	public void setPublicKey(byte[] publicKey) {
		Log.d(TAG, "Setting public key: " + Arrays.toString(publicKey));
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString("public_key", Base64.getEncoder().encodeToString(publicKey));
		editor.commit();
	}

	public byte[] getPublicKey() {
		byte[] publicKey = Base64.getDecoder().decode(preferences.getString("public_key", ""));
		Log.d(TAG, "Got public key: " + Arrays.toString(publicKey) + " (length: " + publicKey.length + ")");
		return publicKey;
	}
	//-------------------------------------------Obfuscated Private Key-------------------------------------------------
	@SuppressLint("ApplySharedPref")
	public void setObfuscatedPrivateKey(byte[] obfuscatedPrivateKey) {
		Log.d(TAG, "Setting obfuscated private key: " + Arrays.toString(obfuscatedPrivateKey));
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString("obfuscated_private_key", Base64.getEncoder().encodeToString(obfuscatedPrivateKey));
		editor.commit();
	}

	public byte[] getObfuscatedPrivateKey() {
		byte[] obfPrivateKey = Base64.getDecoder().decode(preferences.getString("obfuscated_private_key", ""));
		Log.d(TAG, "Got obfuscated private key: " + Arrays.toString(obfPrivateKey) + "(length: " + obfPrivateKey.length + ")");
		return obfPrivateKey;
	}
	//---------------------------------------------Last Query Epoch-----------------------------------------------------

	@SuppressLint("ApplySharedPref")
	public void setLastQueryEpoch(long epoch) {
		Log.d(TAG, "Setting last query epoch: " + epoch);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putLong("last_query_epoch", epoch);
		editor.commit();
	}

	@SuppressLint("ApplySharedPref")
	public long getLastQueryEpoch() {
		if (!preferences.contains("last_query_epoch")) {
			SharedPreferences.Editor editor = preferences.edit();
			editor.putLong("last_query_epoch", 0);
			editor.commit();
		}
		long epoch = preferences.getLong("last_query_epoch", 0);
		Log.d(TAG, "Got last query epoch: " + epoch);
		return epoch;
	}
}
