package com.example.app.bluetooth;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import lombok.Getter;

@Getter
public class ConnectedDevice {
    private BluetoothGatt gatt;
    private BluetoothGattCharacteristic characteristic;

    public ConnectedDevice(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic characteristic) {
        this.gatt = bluetoothGatt;
        this.characteristic = characteristic;
    }
}
