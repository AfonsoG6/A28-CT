package com.example.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.app.ContactTracingService;
import com.example.app.HubFrontend;
import com.example.app.R;

public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		startForegroundService(new Intent(this, ContactTracingService.class));
	}

	public void onClickClaimInfection(View view) {
		Intent intent = new Intent(this, ICCActivity.class);
		startActivity(intent);
	}
}