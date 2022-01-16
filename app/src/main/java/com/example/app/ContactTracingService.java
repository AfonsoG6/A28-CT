package com.example.app;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;
import com.example.app.alarms.QueryInfectedSKsAlarm;
import com.example.app.alarms.SendContactMsgAlarm;
import com.example.app.alarms.SendDummyICCMsgAlarm;
import com.example.app.exceptions.DatabaseInsertionFailedException;

import java.security.NoSuchAlgorithmException;

public class ContactTracingService extends Service {
	private OutgoingMsgManager outMsgManager;
	private IncomingMsgManager inMsgManager;

	@Override
	public void onCreate() {
		super.onCreate();
		try {
			outMsgManager = new OutgoingMsgManager(getApplicationContext());
			inMsgManager = new IncomingMsgManager();
		}
		catch (NoSuchAlgorithmException | DatabaseInsertionFailedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		setAlarms();
		return super.onStartCommand(intent, flags, startId);
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private void setAlarms() {
		QueryInfectedSKsAlarm.setAlarm(this);
		SendContactMsgAlarm.setAlarm(this);
		SendDummyICCMsgAlarm.setAlarm(this);
	}

	public IncomingMsgManager getIncomingMsgManager() {
		return inMsgManager;
	}

	public OutgoingMsgManager getOutgoingMsgManager() {
		return outMsgManager;
	}

}
