package com.example.app;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	public void onPasswordSubmit(View view) {
		try {
			EditText passwordET = findViewById(R.id.passwordTextBox);
			HubFrontend frontend = HubFrontend.getInstance(getApplicationContext());
			String pingResponseString = frontend.ping(passwordET.getText().toString());
			passwordET.setText(pingResponseString);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}