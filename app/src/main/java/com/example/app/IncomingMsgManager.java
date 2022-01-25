package com.example.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import androidx.core.content.ContextCompat;
import com.example.app.activities.AskPasswordActivity;
import com.example.app.activities.MainActivity;
import com.example.app.bluetooth.BleMessage;
import com.example.app.helpers.DatabaseHelper;
import com.example.app.helpers.EpochHelper;
import com.example.app.helpers.SKHelper;
import com.example.app.helpers.SharedPrefsHelper;
import com.example.hub.grpc.Hub;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IncomingMsgManager {
    private static final String TAG = IncomingMsgManager.class.getName();

    private final Context context;

    public IncomingMsgManager(Context context) {
        this.context = context;
    }

    public boolean addMessageToDatabase(byte[] msg, long intervalN) {
        try (DatabaseHelper dbHelper = new DatabaseHelper(context)) {
            Location location = getCurrentLocation();
            SecureStorageManager storageManager = new SecureStorageManager(context);
            byte[] encLat;
            byte[] encLong;
            if (location != null) {
                Log.d(TAG, "Encrypting Latitude: " + location.getLatitude());
                encLat = storageManager.encryptValue(location.getLatitude());
                Log.d(TAG, "Encrypting Longitude: " + location.getLongitude());
                encLong = storageManager.encryptValue(location.getLongitude());
            }
            else {
                encLat = new byte[0];
                encLong = new byte[0];
            }
            Log.i(TAG, "Writing received message: " + msg);
            return dbHelper.insertRecvdMessage(msg, intervalN, encLat, encLong);
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addMessageToDatabase(byte[] data) {
        BleMessage message = BleMessage.fromByteArray(data);
        return addMessageToDatabase(message.getMessage(), message.getIntervalN());
    }

    private Location getCurrentLocation() {
        Log.d(TAG, "Getting current location");
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        return context.getSystemService(LocationManager.class).getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }

    private boolean inRiskOfInfection(List<Hub.SKEpochDayPair> extPairs) throws IOException, NoSuchAlgorithmException {
        Log.d(TAG, "Checking if there is a risk of infection");
        int numContacts;
        try (DatabaseHelper dbHelper = new DatabaseHelper(context)) {
            dbHelper.deleteOldRecvdMsgs();
            numContacts = dbHelper.getNumContacts();
        }
        for(Hub.SKEpochDayPair pair : extPairs) {
            Log.d(TAG, "Checking if there is a risk of infection originating from sk: " + pair.getSk() + " (epochDay: " + pair.getEpochDay() + ")");
            long epochDay = pair.getEpochDay();
            byte[] sk = pair.getSk().toByteArray();
            long firstIntervalN = EpochHelper.getFirstIntervalOfDay(epochDay);
            long lastIntervalN = EpochHelper.getLastIntervalOfDay(epochDay);

            for (long intervalN = firstIntervalN; intervalN <= lastIntervalN; intervalN++) {
                byte[] currentMsg = SKHelper.generateMsg(sk, intervalN);
                try (DatabaseHelper dbHelper = new DatabaseHelper(context)) {
                    if (dbHelper.existsRecvdMessage(currentMsg, intervalN)) {
                        Log.d(TAG, "Found an infected message in internal database: " + Arrays.toString(currentMsg) + " (intervalN: " + intervalN + ")");
                        dbHelper.markContactMsgInfected(currentMsg, intervalN);
                        numContacts++;
                    }
                }
            }
        }
        return numContacts >= 6;
    }

    public static void sendInfectionNotification(Context context) {
        Intent notificationIntent = new Intent(context, AskPasswordActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        NotificationChannel channel = new NotificationChannel("ct_infection", context.getText(R.string.ct_infection_title), NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(context.getText(R.string.ct_infection_text).toString());
        channel.enableLights(true);
        channel.setLightColor(Color.BLUE);
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        Notification notification = new Notification.Builder(context, channel.getId())
                .setOngoing(true)
                .setSmallIcon(R.mipmap.a28_ct)
                .setContentTitle(context.getText(R.string.ct_infection_title))
                .setContentText(context.getText(R.string.ct_infection_text))
                .setContentIntent(pendingIntent)
                .setTicker(context.getText(R.string.ct_infection_ticker))
                .build();

        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(30, notification);
        Log.i(TAG, "Sent infection notification");
    }

    public boolean queryInfectedSks(boolean doNotify) {
        List<Hub.SKEpochDayPair> extSks = null;
        try {
            SharedPrefsHelper spHelper = new SharedPrefsHelper(context);
            Log.d(TAG, "Querying Hub for infected SKs");
            HubFrontend frontend = HubFrontend.getInstance(context);
            Hub.QueryInfectedSKsResponse response = frontend.queryInfectedSKs(spHelper.getLastQueryEpoch());
            spHelper.setLastQueryEpoch(response.getQueryEpoch());
            extSks = response.getSksList();
        }
        catch (RuntimeException | CertificateException | NoSuchAlgorithmException | KeyStoreException | IOException | KeyManagementException e) {
            Log.e(TAG, "Error querying Hub for infected SKs", e);
            e.printStackTrace();
            return false;
        }
        try {
            if (extSks != null) extSks = new ArrayList<>();
            boolean inRisk = inRiskOfInfection(extSks);
            if (inRisk && doNotify) {
                IncomingMsgManager.sendInfectionNotification(context);
            }
            return inRisk;
        } catch (IOException | NoSuchAlgorithmException e) {
            Log.e(TAG, "Error querying Hub for infected SKs", e);
            e.printStackTrace();
            return false;
        }
    }
}
