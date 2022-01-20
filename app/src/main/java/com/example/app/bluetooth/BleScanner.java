package com.example.app.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.*;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import static com.example.app.Constants.SERVICE_UUID;

public class BleScanner {

    private BluetoothLeScanner scanner;
    private ScanSettings scanSettings;
    private List<ScanFilter> filters;
    private List<BluetoothDevice> devices;

    public static int SCAN_PERIOD = 1000; // 1 second

    private ScanCallback leScanCallback =
            new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    BluetoothDevice device = result.getDevice();
                    Log.i("Scan Callback", "Found device: " + device.getName() + " " + device.getAddress());
                    devices.add(result.getDevice());
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
        devices = new ArrayList<>();
    }

    public void startScan() {
        devices.clear();
        scanner.startScan(filters, scanSettings, leScanCallback);
    }

    public void stopScan() {
        scanner.stopScan(leScanCallback);
    }

    public List<BluetoothDevice> getScanResults() {
        return this.devices;
    }
}
