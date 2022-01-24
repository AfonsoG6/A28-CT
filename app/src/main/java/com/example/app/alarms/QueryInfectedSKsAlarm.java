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

import java.util.Calendar;

public class QueryInfectedSKsAlarm extends BroadcastReceiver {
	public static final long INTERVAL = 4 * AlarmManager.INTERVAL_HOUR;

	ContactTracingService service;

	public QueryInfectedSKsAlarm() { /* Empty */ }

	public QueryInfectedSKsAlarm(ContactTracingService service) {
		this.service = service;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("QueryInfectedSKsAlarm", "onReceive");
		Toast.makeText(context, "QueryInfectedSKsAlarm", Toast.LENGTH_LONG).show();
		service.getIncomingMsgManager().queryInfectedSks();
		setAlarm(context);
	}



	public void setAlarm(Context context) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());

		Intent intent = new Intent(context, QueryInfectedSKsAlarm.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
				calendar.getTimeInMillis(),
				INTERVAL,
				pendingIntent
		);
	}
}
