package com.example.app.activities.utils;

import android.text.InputFilter;
import android.text.Spanned;

import java.util.regex.Pattern;

public class PasswordInputFilter implements InputFilter {
	private static final String TAG = PasswordInputFilter.class.getName();

	private static final Pattern ALLOWED_CHARS_PATTERN = Pattern.compile("[a-zA-Z0-9?!@#$%&*_^=]");

	@Override
	public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend)  {
		StringBuilder replacementBuilder = new StringBuilder();
		for (int i=start; i<end; i++) {
			char c = source.charAt(i);
			if (isCharValid(c)) {
				replacementBuilder.append(c);
			}
		}
		return replacementBuilder.toString();
	}

	private static boolean isCharValid(char c) {
		return ALLOWED_CHARS_PATTERN.matcher(String.valueOf(c)).matches();
	}

	public static boolean containsNumber(String password) {
		for (char c : password.toCharArray()) {
			if (Character.isDigit(c)) {
				return true;
			}
		}
		return false;
	}

	public static boolean containsLowerCase(String password) {
		for (char c : password.toCharArray()) {
			if (Character.isLowerCase(c)) {
				return true;
			}
		}
		return false;
	}

	public static boolean containsUpperCase(String password) {
		for (char c : password.toCharArray()) {
			if (Character.isUpperCase(c)) {
				return true;
			}
		}
		return false;
	}

	public static boolean containsSpecialCharacter(String password) {
		String specialCharacters = "?!@#$%&*_^=";
		for (char c : password.toCharArray()) {
			if (specialCharacters.contains(String.valueOf(c))) {
				return true;
			}
		}
		return false;
	}

	// Method to check if a string contains a number, lower case letter, upper case letter and special character
	public static boolean containsAllNeededCharTypes(String password) {
		return containsNumber(password) && containsLowerCase(password) && containsUpperCase(password) && containsSpecialCharacter(password);
	}

}
