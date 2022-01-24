package com.example.app;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.view.View;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.example.app.activities.MainActivity;
import com.example.app.bluetooth.BleMessage;
import com.example.app.helpers.DatabaseHelper;
import com.example.hub.grpc.Hub;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class IncomingMsgManager {
	//TODO: Implement Receiving Contact Messages
	//TODO: Implement Insert received (msg, intervalN) in Database
	//TODO: Implement Store current location when message received
	//TODO: Implement Query Hub for Infected SKs
	//TODO: Generate all Msgs from received SKs
	//TODO: Search database for matching entries
	//TODO: Send notification of infection to user

    private final Context context;
    private static final int NUM_OF_INTERVALS = 288;
    private static final int SECONDS_TO_UPDATE_MSG = 300; // 5min = 5*60s
    private static final int SECONDS_IN_DAY = 86400;
    private static final int SK_DELETED_AFTER_DAYS = 14;

    public IncomingMsgManager(Context context) {
        this.context = context;
    }

    private long getEpochTime() {
        return Calendar.getInstance().getTimeInMillis()/1000;
    }

    private long getEpochDay() {
        return getEpochTime()/SECONDS_IN_DAY;
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

    private boolean hadContact(ArrayList<DatabaseHelper.receivedMsg> msgList, List<Hub.SKEpochDayPair> extPairs) throws IOException, NoSuchAlgorithmException {
        for(Hub.SKEpochDayPair pair : extPairs) {
            int num_contacts = 0;
            for(DatabaseHelper.receivedMsg msg : msgList) {
                System.out.println("Received SK:" + " " + new String(pair.getSk().toByteArray(), StandardCharsets.UTF_8));
                System.out.println("\nSQLite SK: " + " " + new String(msg.getMsg(), StandardCharsets.UTF_8));

                int firstIntervalN = pair.getEpochDay()*SECONDS_IN_DAY/SECONDS_TO_UPDATE_MSG;

                for(int i=firstIntervalN; i!=firstIntervalN+NUM_OF_INTERVALS; i++) {
                    byte[] intervalNBytes = ByteBuffer.allocate(8).putLong(i).array();
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
                    outputStream.write(pair.getSk().toByteArray());
                    outputStream.write(intervalNBytes);
                    byte[] toHash = outputStream.toByteArray();
                    MessageDigest digest = MessageDigest.getInstance("SHA-256");
                    byte[] currentMsg = digest.digest(toHash);

                    if(Arrays.equals(currentMsg,msg.getMsg()))
                        num_contacts++;
                }
            }
            System.out.println("Number of contacts: " + num_contacts);
            if (num_contacts >= 6)
                return true;
        }
        return false;
    }

    public boolean queryInfectedSks() {
        System.out.println("Querying");
        try {
            HubFrontend frontend = HubFrontend.getInstance(context);
            Hub.QueryInfectedSKsResponse response = frontend.queryInfectedSKs();
            System.out.println("SIZE OF RECEIVED SKS: "+response.getSksList().size());
            DatabaseHelper dbHelper = new DatabaseHelper(context); //TODO secondary thread

            ArrayList<DatabaseHelper.receivedMsg> localMsgs = dbHelper.getAllSks(getEpochDay());
            List<Hub.SKEpochDayPair> extSks = response.getSksList();
            boolean contact = hadContact(localMsgs, extSks);
            return contact;
        }
        catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | IOException | KeyManagementException e) {
            e.printStackTrace();
            return false;
        }
    }
}
