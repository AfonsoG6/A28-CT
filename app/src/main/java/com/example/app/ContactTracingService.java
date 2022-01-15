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
	private OutgoingMessageManager outMsgManager;
	private IncomingMessageManager inMsgManager;

	@Override
	public void onCreate() {
		super.onCreate();
		try {
			outMsgManager = new OutgoingMessageManager(getApplicationContext());
			inMsgManager = new IncomingMessageManager();
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
		new QueryInfectedSKsAlarm(this, inMsgManager);
		new SendContactMsgAlarm(this, outMsgManager);
		new SendDummyICCMsgAlarm(this);
	}
}
