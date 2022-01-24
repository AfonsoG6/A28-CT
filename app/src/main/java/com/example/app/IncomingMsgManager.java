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
import java.util.List;

public class IncomingMsgManager {
    private static final String TAG = IncomingMsgManager.class.getName();

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
            Log.i(TAG, "Writing received message: " + message.toString());
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

    private boolean inRiskOfInfection(List<Hub.SKEpochDayPair> extPairs) throws IOException, NoSuchAlgorithmException {
        int numContacts;
        try (DatabaseHelper dbHelper = new DatabaseHelper(context)) {
            dbHelper.deleteOldRecvdMsgs();
            numContacts = dbHelper.getNumContacts();
        }
        for(Hub.SKEpochDayPair pair : extPairs) {
            long epochDay = pair.getEpochDay();
            byte[] sk = pair.getSk().toByteArray();
            long firstIntervalN = EpochHelper.getFirstIntervalOfDay(epochDay);
            long lastIntervalN = EpochHelper.getLastIntervalOfDay(epochDay);

            for (long intervalN = firstIntervalN; intervalN <= lastIntervalN; intervalN++) {
                byte[] currentMsg = SKHelper.generateMsg(sk, intervalN);
                try (DatabaseHelper dbHelper = new DatabaseHelper(context)) {
                    if (dbHelper.existsRecvdMessage(currentMsg, intervalN)) {
                        dbHelper.updateContact(currentMsg, intervalN);
                        numContacts++;
                    }
                }
            }
        }
        return numContacts >= 6;
    }

    private void sendInfectionNotification() {
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        NotificationChannel channel = new NotificationChannel("ct_infection", context.getText(R.string.ct_infection_title), NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(context.getText(R.string.ct_infection_text).toString());
        channel.enableLights(true);
        channel.setLightColor(Color.BLUE);
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        Notification notification = new Notification.Builder(context, channel.getId())
                .setOngoing(true)
                .setContentTitle(context.getText(R.string.ct_infection_title))
                .setContentText(context.getText(R.string.ct_infection_text))
                .setContentIntent(pendingIntent)
                .setTicker(context.getText(R.string.ct_infection_ticker))
                .build();

        synchronized (this) {
            notification.notifyAll();
        }
    }

    public boolean queryInfectedSks() {
        try {
            SharedPrefsHelper spHelper = new SharedPrefsHelper(context);
            HubFrontend frontend = HubFrontend.getInstance(context);
            Hub.QueryInfectedSKsResponse response = frontend.queryInfectedSKs(spHelper.getLastQueryEpoch());
            spHelper.setLastQueryEpoch(response.getQueryEpoch());

            List<Hub.SKEpochDayPair> extSks = response.getSksList();
            boolean inRisk = inRiskOfInfection(extSks);
            if (inRisk) {
                sendInfectionNotification();
            }
            return inRisk;
        }
        catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | IOException | KeyManagementException e) {
            e.printStackTrace();
            return false;
        }
    }
}
