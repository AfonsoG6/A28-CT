package com.example.app.alarms;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;
import com.example.app.ContactTracingService;
import com.example.app.HubFrontend;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Calendar;
import java.util.Random;

public class SendDummyICCMsgAlarm extends BroadcastReceiver {
	private static final Random random = new Random();

	ContactTracingService service;

	public SendDummyICCMsgAlarm() { /* Empty */ }

	public SendDummyICCMsgAlarm(ContactTracingService service) {
		this.service = service;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("SendDummyICCMsgAlarm", "onReceive");
		Toast.makeText(context, "SendDummyICCMsgAlarm", Toast.LENGTH_LONG).show();
		service.sendDummyICCMsg();
		setAlarm(context);
	}

	public void setAlarm(Context context) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis() + getRandomInterval());

		Intent intent = new Intent(context, SendContactMsgAlarm.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 2, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.set(AlarmManager.RTC_WAKEUP,
				calendar.getTimeInMillis(),
				pendingIntent
		);
	}

	private long getRandomInterval() {
		// We are using a simple mathematical function to make small intervals less likely while still keeping them possible.
		double exp = random.nextDouble() * 4.6; // Random Value between 0 and 4.6
		return Math.round((24-Math.pow(2, exp))*AlarmManager.INTERVAL_HOUR);
	}
}
