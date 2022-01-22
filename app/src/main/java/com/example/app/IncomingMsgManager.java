package com.example.app;

import android.content.Context;
import com.example.app.bluetooth.BleMessage;
import com.example.app.database.DatabaseHelper;

public class IncomingMsgManager {
	//TODO: Implement Receiving Contact Messages
	//TODO: Implement Insert received (msg, intervalN) in Database
	//TODO: Implement Store current location when message received
	//TODO: Implement Query Hub for Infected SKs
	//TODO: Generate all Msgs from received SKs
	//TODO: Search database for matching entries
	//TODO: Send notification of infection to user

    private Context context;

    public IncomingMsgManager(Context context) {
        this.context = context;
    }

    public boolean addMessageToDatabase(byte[] data) {
        try (DatabaseHelper dbHelper = new DatabaseHelper(context)) {
            BleMessage message = BleMessage.fromByteArray(data);
            return dbHelper.insertRecvdMessage(message.getMessage(), message.getIntervalN());
        }
    }
}
