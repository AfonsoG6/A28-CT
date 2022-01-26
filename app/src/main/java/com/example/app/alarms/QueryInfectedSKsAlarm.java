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

public class QueryInfectedSKsAlarm extends BroadcastReceiver {
	private static final String TAG = QueryInfectedSKsAlarm.class.getName();
	private static final String ACTION_ALARM = "ALARM";
	private static final long INTERVAL = 4 * AlarmManager.INTERVAL_HOUR;

	private ContactTracingService service;

	public QueryInfectedSKsAlarm() { /* Empty */ }

	public QueryInfectedSKsAlarm(ContactTracingService service) {
		this.service = service;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "Received Broadcast (" + intent.getAction() + ")");
		if (intent.getAction().equals(ACTION_ALARM)) {
			// Called by the AlarmManager
			Log.i(TAG, "Received alarm");
			Intent queryIntent = new Intent(ContactTracingService.ACTION_QUERY_INFECTED_SKS);
			LocalBroadcastManager.getInstance(context).sendBroadcast(queryIntent);
		}
		else if (intent.getAction().equals(ContactTracingService.ACTION_QUERY_INFECTED_SKS)) {
			// Called by the LocalBroadcastManager
			Toast.makeText(context, "Querying Hub for Infected SKs", Toast.LENGTH_LONG).show();
			service.queryInfectedSks();
		}
	}

	public void setAlarm(Context context) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());

		Intent intent = new Intent(context, QueryInfectedSKsAlarm.class);
		intent.setAction(ACTION_ALARM);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
				calendar.getTimeInMillis(),
				INTERVAL,
				pendingIntent
		);
	}

	public IntentFilter getIntentFilter() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(ContactTracingService.ACTION_QUERY_INFECTED_SKS);
		return filter;
	}
}
