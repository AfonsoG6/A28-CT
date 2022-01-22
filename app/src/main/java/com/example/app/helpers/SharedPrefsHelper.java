package com.example.app.helpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.Base64;

public class SharedPrefsHelper {
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
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString("check_salt", Base64.getEncoder().encodeToString(salt));
		editor.commit();
	}

	public byte[] getCheckSalt() {
		return Base64.getDecoder().decode(preferences.getString("check_salt", ""));
	}
	//-----------------------------------------------Obfuscation Salt---------------------------------------------------
	@SuppressLint("ApplySharedPref")
	public void setObfuscationSalt(byte[] salt) {
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString("obfuscation_salt", Base64.getEncoder().encodeToString(salt));
		editor.commit();
	}

	public byte[] getObfuscationSalt() {
		return Base64.getDecoder().decode(preferences.getString("obfuscation_salt", ""));
	}
	//--------------------------------------------------Check Hash------------------------------------------------------
	@SuppressLint("ApplySharedPref")
	public void setCheckHash(byte[] hashedPassword) {
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString("check_hash", Base64.getEncoder().encodeToString(hashedPassword));
		editor.commit();
	}

	public byte[] getCheckHash() {
		return Base64.getDecoder().decode(preferences.getString("check_hash", ""));
	}

	//-------------------------------------------------Public Key-------------------------------------------------------
	@SuppressLint("ApplySharedPref")
	public void setPublicKey(byte[] publicKey) {
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString("public_key", Base64.getEncoder().encodeToString(publicKey));
		editor.commit();
	}

	public byte[] getPublicKey() {
		return Base64.getDecoder().decode(preferences.getString("public_key", ""));
	}
	//-------------------------------------------Obfuscated Private Key-------------------------------------------------
	@SuppressLint("ApplySharedPref")
	public void setObfuscatedPrivateKey(byte[] obfuscatedPrivateKey) {
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString("obfuscated_private_key", Base64.getEncoder().encodeToString(obfuscatedPrivateKey));
		editor.commit();
	}

	public byte[] getObfuscatedPrivateKey() {
		return Base64.getDecoder().decode(preferences.getString("obfuscated_private_key", ""));
	}

}
