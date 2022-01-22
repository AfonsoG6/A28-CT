package com.example.app.activities;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.app.ContactTracingService;
import com.example.app.R;

public class MainActivity extends AppCompatActivity {
	private static final String TAG = MainActivity.class.getName();
	private static final int REQUEST_ENABLE_BT = 10;
	private static final int REQUEST_GIVE_LOCATION_PERMISSION = 11;
	private static final int REQUEST_ENABLE_LOCATION = 12;

	private BluetoothAdapter adapter;
	private boolean hasLocationPermission;

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
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_ENABLE_BT && resultCode != RESULT_OK) {
			promptEnableBluetooth();
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