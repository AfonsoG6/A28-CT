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

public class SendContactMsgAlarm extends BroadcastReceiver {
	private static final String TAG = SendContactMsgAlarm.class.getName();
	private static String ACTION_ALARM = "ALARM";
	private static final long INTERVAL = AlarmManager.INTERVAL_FIFTEEN_MINUTES/15;

	private ContactTracingService service;

	public SendContactMsgAlarm() { /* Empty */ }

	public SendContactMsgAlarm(ContactTracingService service) {
		this.service = service;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "Received Broadcast (" + intent.getAction() + ")");
		if (intent.getAction().equals(ACTION_ALARM)) {
			// Called by the AlarmManager
			Intent sendIntent = new Intent(context, ContactTracingService.class);
			sendIntent.setAction(ContactTracingService.ACTION_SEND_CONTACT_MSG);
			LocalBroadcastManager.getInstance(context).sendBroadcast(sendIntent);
		}
		else if (intent.getAction().equals(ContactTracingService.ACTION_SEND_CONTACT_MSG)) {
			// Called by the LocalBroadcastManager
			Toast.makeText(context, "Sending Contact Msg", Toast.LENGTH_LONG).show();
			service.sendContactMsg();
		}
	}

	public void setAlarm(Context context) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());

		Intent intent = new Intent(context, SendContactMsgAlarm.class);
		intent.setAction(ACTION_ALARM);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
				calendar.getTimeInMillis(),
				INTERVAL,
				pendingIntent
		);
	}

	public IntentFilter getIntentFilter() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(ContactTracingService.ACTION_SEND_CONTACT_MSG);
		return filter;
	}
}
