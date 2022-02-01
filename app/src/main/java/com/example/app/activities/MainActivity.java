package com.example.app.activities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.app.ContactTracingService;
import com.example.app.IncomingMsgManager;
import com.example.app.OutgoingMsgManager;
import com.example.app.R;
import com.example.app.exceptions.DatabaseInsertionFailedException;
import com.example.app.exceptions.NotFoundInDatabaseException;
import com.example.app.helpers.EpochHelper;
import com.example.app.helpers.SKHelper;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity {
	private static final String TAG = MainActivity.class.getName();
	private static final int REQUEST_ENABLE_BT = 10;
	private static final int REQUEST_GIVE_LOCATION_PERMISSION = 11;

	private BluetoothAdapter adapter;
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

	public void onClickDemoButton(View view) {
		try {
			OutgoingMsgManager omm = new OutgoingMsgManager(this.getApplicationContext());
			byte[] sk = omm.getSK(this.getApplicationContext(), EpochHelper.getCurrentEpochDay());
			for (int i=0; i<6; i++) {
				long intervalN = EpochHelper.getCurrentInterval() + i;
				imm.addMessageToDatabase(SKHelper.generateMsg(sk, intervalN), intervalN);
			}
			Toast.makeText(this.getApplicationContext(), "6 own messages added to database", Toast.LENGTH_SHORT).show();
		} catch (NoSuchAlgorithmException | DatabaseInsertionFailedException | IOException | NotFoundInDatabaseException e) {
			e.printStackTrace();
			Toast.makeText(this.getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	public void onClickCheckInfection(View view) {
		statusTextView.setTextColor(Color.BLACK);
		statusTextView.setText("Checking exposed status...");
		statusTextView.setVisibility(View.VISIBLE);

		boolean isInfected = imm.queryInfectedSks(false);

		if (isInfected) {
			statusTextView.setTextColor(Color.RED);
			statusTextView.setText("Possible Infection!");
			findViewById(R.id.checkContactsButton).setEnabled(true);
		} else {
			statusTextView.setTextColor(Color.GREEN);
			statusTextView.setText("No detected contact.");
			findViewById(R.id.checkContactsButton).setEnabled(false);
		}
	}

	public void onClickCheckThreats(View view) {
		Intent intent = new Intent(this, AskPasswordActivity.class);
		startActivity(intent);
	}

	public void onClickClaimInfection(View view) {
		Intent intent = new Intent(this, ICCActivity.class);
		startActivity(intent);
	}

	private void promptEnableBluetooth() {
		if (!adapter.isEnabled()) {
			Log.i(TAG, "Bluetooth is not enabled. Prompting user to enable it.");
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
	}

	private void requestLocationPermission() {
		if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			Log.i(TAG, "Location permission not granted. Requesting it.");
			ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_GIVE_LOCATION_PERMISSION);
		}
	}
}