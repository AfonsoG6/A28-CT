package com.example.app.alarms;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import com.example.app.IncomingMessageManager;

public class QueryInfectedSKsAlarm extends BroadcastReceiver {
	public static final long INTERVAL = 4 * AlarmManager.INTERVAL_HOUR;

	private final IncomingMessageManager inMsgManager;

	public QueryInfectedSKsAlarm(Context context, IncomingMessageManager msgManager) {
		inMsgManager = msgManager;

		setAlarm(context);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		//TODO use inMsgManager to query Infected SKs and check the database for matching msgs
	}

	private void setAlarm(Context context) {
		Intent intent = new Intent(context, QueryInfectedSKsAlarm.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
				SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_FIFTEEN_MINUTES,
				INTERVAL,
				pendingIntent
		);
	}
}
