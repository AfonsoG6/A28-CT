package com.example.app.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.app.R;
import com.example.app.SecureStorageManager;
import com.example.app.activities.utils.PasswordInputFilter;
import com.example.app.exceptions.PasswordSetFailedException;
import com.example.app.helpers.SharedPrefsHelper;

import java.security.NoSuchAlgorithmException;

public class SetPasswordActivity extends AppCompatActivity {
	private static final String TAG = SetPasswordActivity.class.getName();
	private static final int MIN_PASSWORD_LENGTH = 8;

	private TextView passwordFeedback;
	private EditText passwordEditText;
	private EditText confirmPasswordEditText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SharedPrefsHelper spHelper = new SharedPrefsHelper(getApplicationContext());
		if (spHelper.hasPasswordSet()) {
			Log.i(TAG, "Password already set, redirecting to main activity");
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);
			finish();
			return;
		}

		setContentView(R.layout.activity_set_password);

		passwordFeedback = findViewById(R.id.newPasswordFeedback);
		passwordEditText = findViewById(R.id.newPasswordEditText);
		confirmPasswordEditText = findViewById(R.id.confirmNewPasswordEditText);
		InputFilter[] filters = {new PasswordInputFilter()};
		passwordEditText.setFilters(filters);
		confirmPasswordEditText.setFilters(filters);
	}

	public void onClickSubmitNewPassword(View view) {
		String password = passwordEditText.getText().toString();
		String confirmPassword = confirmPasswordEditText.getText().toString();
		passwordFeedback.setTextColor(Color.RED);
		if (password.length() < MIN_PASSWORD_LENGTH) {
			Log.d(TAG, "Password '" + password + "' is too short (" + password.length() + " < " + MIN_PASSWORD_LENGTH + ")");
			passwordFeedback.setText("Password must be at least "+ MIN_PASSWORD_LENGTH + " characters long");
			passwordFeedback.setVisibility(View.VISIBLE);
			return;
		}
		if (!PasswordInputFilter.containsAllNeededCharTypes(password)) {
			Log.d(TAG, "Password '" + password + "' does not contain all needed character types");
			passwordFeedback.setText("Password must contain at least one:\n- Uppercase letter\n- Lowercase letter\n- Number\n- Special character");
			passwordFeedback.setVisibility(View.VISIBLE);
			return;
		}
		if (!password.equals(confirmPassword)) {
			Log.d(TAG, "Passwords do not match (" + password + " != " + confirmPassword + ")");
			passwordFeedback.setText("Passwords do not match");
			passwordFeedback.setVisibility(View.VISIBLE);
			return;
		}
		try {
			new SecureStorageManager(this).setPassword(password);
		}
		catch (PasswordSetFailedException | NoSuchAlgorithmException e) {
			e.printStackTrace();
			Log.e(TAG, "Failed to set password due to " + e.getMessage());
			passwordFeedback.setText("Internal error occurred");
			passwordFeedback.setVisibility(View.VISIBLE);
			return;
		}
		Toast.makeText(this, "Password set successfully", Toast.LENGTH_SHORT).show();
		Log.d(TAG, "Password set successfully (" + password + ")");
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		finish();
	}
}
