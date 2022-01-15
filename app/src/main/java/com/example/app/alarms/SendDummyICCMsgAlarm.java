package com.example.app.alarms;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import com.example.app.HubFrontend;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Random;

public class SendDummyICCMsgAlarm extends BroadcastReceiver {
	private final Random random = new Random();

	public SendDummyICCMsgAlarm(Context context) {
		setAlarm(context);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			HubFrontend hubFrontend = HubFrontend.getInstance(context);
			hubFrontend.sendDummyClaimInfection();
		}
		catch (NoSuchAlgorithmException | CertificateException | KeyStoreException | IOException | KeyManagementException e) {
			e.printStackTrace();
		}
		setAlarm(context);
	}

	private void setAlarm(Context context) {
		Intent intent = new Intent(context, SendContactMsgAlarm.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.set(AlarmManager.ELAPSED_REALTIME,
				SystemClock.elapsedRealtime() + getRandomInterval(),
				pendingIntent
		);
	}

	private long getRandomInterval() {
		// We are using a simple mathematical function to make small intervals less likely while still keeping them possible.
		double exp = random.nextDouble() * 4.6; // Random Value between 0 and 4.6
		return Math.round((24-Math.pow(2, exp))*AlarmManager.INTERVAL_HOUR);
	}
}
