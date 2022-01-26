package com.example.app.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.*;
import android.os.ParcelUuid;
import android.util.Log;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

import static com.example.app.Constants.SERVICE_UUID;

public class BleScanner {

    private static final String TAG = BleScanner.class.getName();

    private BluetoothLeScanner scanner;
    private ScanSettings scanSettings;
    private List<ScanFilter> filters;
    @Getter private List<BluetoothDevice> scanResults;

    private ScanCallback leScanCallback =
            new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    BluetoothDevice device = result.getDevice();
                    Log.i(TAG, "Found device: " + device.getAddress());
                    scanResults.add(result.getDevice());
                }
            };

    public BleScanner(BluetoothAdapter adapter) {
        ScanFilter filter = new ScanFilter.Builder().setServiceUuid(
                new ParcelUuid(SERVICE_UUID)).build();
        filters = new ArrayList<>();
        filters.add(filter);
        scanner = adapter.getBluetoothLeScanner();
        scanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_FIRST_MATCH)
                .build();
        scanResults = new ArrayList<>();
    }

    public void startScan() {
        Log.i(TAG, "Starting BLE scan");
        scanResults.clear();
        scanner.startScan(filters, scanSettings, leScanCallback);
    }

    public void stopScan() {
        scanner.stopScan(leScanCallback);
    }
}
