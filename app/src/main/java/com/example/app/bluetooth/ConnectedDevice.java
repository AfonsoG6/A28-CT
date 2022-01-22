package com.example.app.bluetooth;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

public class ConnectedDevice {
    private BluetoothGatt gatt;
    private BluetoothGattCharacteristic characteristic;

    public ConnectedDevice(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic characteristic) {
        this.gatt = bluetoothGatt;
        this.characteristic = characteristic;
    }

    public BluetoothGatt getGatt() {
        return gatt;
    }

    public BluetoothGattCharacteristic getCharacteristic() {
        return characteristic;
    }
}
