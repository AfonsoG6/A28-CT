package com.example.app.activities;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.app.ContactTracingService;
import com.example.app.HubFrontend;
import com.example.app.IncomingMsgManager;
import com.example.app.R;
import com.example.app.helpers.DatabaseHelper;
import com.example.hub.grpc.Hub;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {
	private static final String TAG = MainActivity.class.getName();
	private static final int REQUEST_ENABLE_BT = 10;
	private static final int REQUEST_GIVE_LOCATION_PERMISSION = 11;
	private static final int REQUEST_ENABLE_LOCATION = 12;

	private BluetoothAdapter adapter;
	private boolean hasLocationPermission;
	private TextView statusTextView;

	private IncomingMsgManager imm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		startForegroundService(new Intent(this, ContactTracingService.class));
		adapter = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
		if (adapter == null)
			return;
		promptEnableBluetooth();
		hasLocationPermission = ContextCompat.checkSelfPermission(this,
				Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
		requestLocationPermission();
		statusTextView = findViewById(R.id.exposedStatusText);
		imm = new IncomingMsgManager(this.getApplicationContext());
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_ENABLE_BT && resultCode != RESULT_OK) {
			promptEnableBluetooth();
		}
	}

	public void onClickCheckInfection(View view) {
		statusTextView.setTextColor(getResources().getColor(R.color.black, getTheme()));
		statusTextView.setText("Checking exposed status...");
		statusTextView.setVisibility(View.VISIBLE);

		DatabaseHelper db = new DatabaseHelper(this.getApplicationContext());
		db.createTable();
		boolean isInfected = imm.queryInfectedSks();

		if (isInfected) {
			statusTextView.setTextColor(getResources().getColor(R.color.red, getTheme()));
			statusTextView.setText("Possible Infection!");
		} else {
			statusTextView.setTextColor(getResources().getColor(R.color.green, getTheme()));
			statusTextView.setText("No detected contact.");
		}


	}


	public void onClickClaimInfection(View view) {
		Intent intent = new Intent(this, ICCActivity.class);
		startActivity(intent);
	}

	private void promptEnableBluetooth() {
		if (!adapter.isEnabled()) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
	}

	private void requestLocationPermission() {
		if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_GIVE_LOCATION_PERMISSION);
		}
	}
}