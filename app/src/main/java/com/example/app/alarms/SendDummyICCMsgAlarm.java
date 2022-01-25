package com.example.app.alarms;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.example.app.ContactTracingService;

import java.util.Calendar;
import java.util.Random;

public class SendDummyICCMsgAlarm extends BroadcastReceiver {
	private static final String TAG = SendDummyICCMsgAlarm.class.getName();
	private static String ACTION_ALARM = "ALARM";
	private static final Random random = new Random();

	private ContactTracingService service;

	public SendDummyICCMsgAlarm() { /* Empty */ }

	public SendDummyICCMsgAlarm(ContactTracingService service) {
		this.service = service;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "Received Broadcast (" + intent.getAction() + ")");
		if (intent.getAction().equals(ACTION_ALARM)) {
			// Called by the AlarmManager
			Log.i(TAG, "Received alarm");
			Intent sendIntent = new Intent(context, ContactTracingService.class);
			sendIntent.setAction(ContactTracingService.ACTION_SEND_DUMMY_ICC_MSG);
			LocalBroadcastManager.getInstance(context).sendBroadcast(sendIntent);
			setAlarm(context);
		}
		else if (intent.getAction().equals(ContactTracingService.ACTION_SEND_DUMMY_ICC_MSG)) {
			// Called by the LocalBroadcastManager
			Toast.makeText(context, "Sending Dummy ICC", Toast.LENGTH_LONG).show();
			service.sendDummyICCMsg();
		}
	}

	public void setAlarm(Context context) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis() + getRandomInterval());

		Intent intent = new Intent(context, SendDummyICCMsgAlarm.class);
		intent.setAction(ACTION_ALARM);
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
		long interval = Math.round((24-Math.pow(2, exp))*AlarmManager.INTERVAL_HOUR);
		Log.d(TAG, "Generated random interval: " + interval + "ms");
		return interval;
	}

	public IntentFilter getIntentFilter() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(ContactTracingService.ACTION_SEND_DUMMY_ICC_MSG);
		return filter;
	}
}
