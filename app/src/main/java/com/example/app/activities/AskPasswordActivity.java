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
import com.example.app.exceptions.PasswordCheckFailedException;

import java.security.NoSuchAlgorithmException;

public class AskPasswordActivity extends AppCompatActivity {
	private static final String TAG = AskPasswordActivity.class.getName();

	private TextView passwordFeedback;
	private EditText passwordEditText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ask_password);

		passwordFeedback = findViewById(R.id.passwordFeedback);
		passwordEditText = findViewById(R.id.passwordEditText);
		InputFilter[] filters = {new PasswordInputFilter()};
		passwordEditText.setFilters(filters);
	}

	public void onClickSubmitPassword(View view) {
		try {
			SecureStorageManager ssManager = new SecureStorageManager(this);
			String password = passwordEditText.getText().toString();
			Log.d(TAG, "User's password attempt: " + password);
			if (ssManager.passwordMatches(password)) {
				Intent intent = new Intent(this, ShowContactsActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("password", password);
				intent.putExtras(bundle);
				Toast.makeText(this, "Password is correct", Toast.LENGTH_SHORT).show();
				Log.d(TAG, "Password is correct, redirecting to ShowContactsActivity");
				startActivity(intent);
				finish();
			}
			else {
				Log.d(TAG, "Password is incorrect");
				passwordFeedback.setText("Password is incorrect");
				passwordFeedback.setTextColor(Color.RED);
				passwordFeedback.setVisibility(View.VISIBLE);
			}
		}
		catch (PasswordCheckFailedException | NoSuchAlgorithmException e) {
			Log.e(TAG, "Error while checking password", e);
			e.printStackTrace();
			passwordFeedback.setText("An internal error occurred");
			passwordFeedback.setTextColor(Color.RED);
			passwordFeedback.setVisibility(View.VISIBLE);
		}
	}
}
