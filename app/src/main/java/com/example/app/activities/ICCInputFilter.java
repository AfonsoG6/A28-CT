package com.example.app.activities;

import android.text.*;

import java.util.regex.Pattern;

public class ICCInputFilter implements InputFilter {
	private static final Pattern allowedCharsPattern = Pattern.compile("[a-zA-Z0-9]");

	@Override
	public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend)  {
		String replacement = source.toString().substring(start, end);
		StringBuilder replacementBuilder = new StringBuilder();
		for (int i=0; i<replacement.length(); i++) {
			String c = "" + replacement.charAt(i);
			if (allowedCharsPattern.matcher(c).matches()) {
				replacementBuilder.append(c);
			}
		}

		String result = getNewSubStrings(replacementBuilder.toString(), dest.toString(), dstart, dend)[1];
		if (result.length() > 23) return result.substring(0, 23);
		else return result;
	}

	public String[] getNewSubStrings(String replacement, String dest, int dstart, int dend)  {
		String[] substrs = new String[3];

		if (dstart > 0) substrs[0] = dest.substring(0, dstart);
		else substrs[0] = "";

		substrs[1] = replacement;

		if (dest.length() > dend) substrs[2] = dest.substring(dend);
		else substrs[2] = "";

		for (int i=0; i<substrs.length; i++) {
			substrs[i] = substrs[i].replace("-", "");
		}

		String[] finalSubstrs = new String[3];
		int count = 0;
		for (int i=0; i<substrs.length; i++) {
			finalSubstrs[i] = "";
			for (char c : substrs[i].toCharArray()) {
				if (count > 0 && count%5 == 0) finalSubstrs[i] += "-";
				finalSubstrs[i] += c;

				count++;
			}
		}

		return finalSubstrs;
	}

}

