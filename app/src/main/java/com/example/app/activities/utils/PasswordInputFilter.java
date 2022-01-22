package com.example.app.activities.utils;

import android.text.InputFilter;
import android.text.Spanned;

import java.util.regex.Pattern;

public class PasswordInputFilter implements InputFilter {

	private static final Pattern allowedCharsPattern = Pattern.compile("[a-zA-Z0-9?!@#$%&*_^=]");

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

	private boolean isCharValid(char c) {
		return allowedCharsPattern.matcher(String.valueOf(c)).matches();
	}

}
