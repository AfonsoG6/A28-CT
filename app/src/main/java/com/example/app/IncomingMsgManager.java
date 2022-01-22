package com.example.app;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import androidx.core.content.ContextCompat;
import com.example.app.bluetooth.BleMessage;
import com.example.app.helpers.DatabaseHelper;

import java.security.NoSuchAlgorithmException;

public class IncomingMsgManager {
	//TODO: Implement Receiving Contact Messages
	//TODO: Implement Insert received (msg, intervalN) in Database
	//TODO: Implement Store current location when message received
	//TODO: Implement Query Hub for Infected SKs
	//TODO: Generate all Msgs from received SKs
	//TODO: Search database for matching entries
	//TODO: Send notification of infection to user

    private final Context context;

    public IncomingMsgManager(Context context) {
        this.context = context;
    }

    public boolean addMessageToDatabase(byte[] data) {
        try (DatabaseHelper dbHelper = new DatabaseHelper(context)) {
            BleMessage message = BleMessage.fromByteArray(data);
            Location location = getCurrentLocation();
            SecureStorageManager storageManager = new SecureStorageManager(context);
            byte[] encLat;
            byte[] encLong;
            if (location != null) {
                encLat = storageManager.encryptValue(location.getLatitude());
                encLong = storageManager.encryptValue(location.getLongitude());
            }
            else {
                encLat = new byte[0];
                encLong = new byte[0];
            }
            return dbHelper.insertRecvdMessage(message.getMessage(), message.getIntervalN(), encLat, encLong);
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Location getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        return context.getSystemService(LocationManager.class).getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }
}
