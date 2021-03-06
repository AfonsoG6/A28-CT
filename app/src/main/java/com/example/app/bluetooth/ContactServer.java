package com.example.app.bluetooth;

import android.bluetooth.*;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;
import com.example.app.IncomingMsgManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.example.app.Constants.MESSAGE_UUID;
import static com.example.app.Constants.SERVICE_UUID;

public class ContactServer {
    private static final String TAG = ContactServer.class.getName();

    private static BluetoothManager manager;
    private static BluetoothAdapter adapter;

    private static BluetoothGattServer gattServer;

    private static AdvertiseCallback advertiseCallback;
    private static final AdvertiseSettings advertiseSettings = buildAdvertiseSettings();
    private static final AdvertiseData advertiseData = buildAdvertiseData();

    private static BluetoothGattCallback gattClientCallback;

    private static final List<ConnectedDevice> connectedDevices = new ArrayList<>();

    private static IncomingMsgManager inMsgManager;

    public static void startServer(Context context) {
        manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (manager == null)
            return;
        adapter = manager.getAdapter();
        setUpGattServer(context);
        startAdvertisement();
    }

    public static void connectDevices(Context context, List<BluetoothDevice> devices) {
        connectedDevices.clear();
        if (gattClientCallback == null)
            gattClientCallback = new GattClientCallback();
        for (BluetoothDevice device: devices) {
            device.connectGatt(context, false, gattClientCallback);
        }
    }

    public static void sendMessage(byte[] message) {
        Log.i(TAG, "Sending message " + Arrays.toString(message));
        for (ConnectedDevice device: connectedDevices) {
            device.getCharacteristic().setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            device.getCharacteristic().setValue(message);

            boolean success = device.getGatt().writeCharacteristic(device.getCharacteristic());
            if (!success) {
                Log.w(TAG, "Could not send message to device: " + device.getGatt().getDevice().getAddress());
            }
        }
    }

    private static void startAdvertisement() {
        BluetoothLeAdvertiser advertiser = adapter.getBluetoothLeAdvertiser();
        if (advertiseCallback == null) {
            advertiseCallback = new DeviceAdvertiseCallback();

            advertiser.startAdvertising(advertiseSettings, advertiseData, advertiseCallback);
        }
    }

    private static void setUpGattServer(Context context) {
        BluetoothGattServerCallback gattServerCallback = new GattServerCallback();
        gattServer = manager.openGattServer(context, gattServerCallback);
        gattServer.addService(setUpGattService());
    }

    private static BluetoothGattService setUpGattService() {
        BluetoothGattService service = new BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY);
        BluetoothGattCharacteristic skCharacteristics = new BluetoothGattCharacteristic(
                MESSAGE_UUID,
                BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_WRITE
        );
        service.addCharacteristic(skCharacteristics);
        return service;
    }

    private static AdvertiseSettings buildAdvertiseSettings() {
        return new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTimeout(0)
                .build();
    }

    private static AdvertiseData buildAdvertiseData() {
        return new AdvertiseData.Builder()
                .addServiceUuid(new ParcelUuid(SERVICE_UUID))
                .setIncludeDeviceName(true)
                .build();
    }

    public static void setInMsgManager(IncomingMsgManager inMsgManager) {
        ContactServer.inMsgManager = inMsgManager;
    }

    private static class GattServerCallback extends BluetoothGattServerCallback {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED)
                Log.i(TAG, "Started connection with client " + device.getAddress());
        }

        @Override
        public void onCharacteristicWriteRequest(
                BluetoothDevice device,
                int requestId,
                BluetoothGattCharacteristic characteristic,
                boolean preparedWrite,
                boolean responseNeeded,
                int offset,
                byte[] value
        ) {
            super.onCharacteristicWriteRequest(
                    device, requestId, characteristic, preparedWrite, responseNeeded, offset, value
            );
            Log.i(TAG, "Received message" + Arrays.toString(value) + " from " + device.getAddress());

            if (characteristic.getUuid() == MESSAGE_UUID) {
                gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null);
                boolean insertResult = inMsgManager.addMessageToDatabase(value);
                if (!insertResult)
                    Log.w(TAG, "Could not write received message to database");
            }
        }
    }

    private static class DeviceAdvertiseCallback extends AdvertiseCallback {
        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            Log.w(TAG, "Could not start Ble Advertisement. Error: " + errorCode);
        }

        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            Log.i(TAG, "Started Ble Advertisement");
        }
    }

    private static class GattClientCallback extends BluetoothGattCallback {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            boolean isSuccess = status == BluetoothGatt.GATT_SUCCESS;
            boolean isConnected = newState == BluetoothProfile.STATE_CONNECTED;
            if (isSuccess && isConnected) {
                Log.i(TAG, "Connected to gatt server: " + gatt.getDevice().getAddress());
                gatt.requestMtu(256);
                gatt.discoverServices();
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            Log.i(TAG, "Changed MTU: " + mtu + " " + status);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService service = gatt.getService(SERVICE_UUID);
                if (service == null) {
                    Log.i(TAG, "Device doesn't have service: " + gatt.getDevice().getAddress());
                    return;
                }
                BluetoothGattCharacteristic characteristic = service.getCharacteristic(MESSAGE_UUID);
                if (characteristic == null) {
                    Log.i(TAG, "Device doesn't have characteristic: " + gatt.getDevice().getAddress());
                    return;
                }
                connectedDevices.add(new ConnectedDevice(gatt, characteristic));
            }
        }
    }
}
