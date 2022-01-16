package com.example.app.alarms;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import com.example.app.ContactTracingService;
import com.example.app.OutgoingMsgManager;
import com.example.app.exceptions.DatabaseInsertionFailedException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class SendContactMsgAlarm extends BroadcastReceiver {
	public static final long INTERVAL = 2*AlarmManager.INTERVAL_FIFTEEN_MINUTES/15;

	public SendContactMsgAlarm() {}

	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			OutgoingMsgManager outMsgManager = context.getSystemService(ContactTracingService.class).getOutgoingMsgManager();
			outMsgManager.sendContactMsg(context);
		} catch (DatabaseInsertionFailedException | NoSuchAlgorithmException | IOException e) {
			e.printStackTrace();
		}
	}

	public static void setAlarm(Context context) {
		Intent intent = new Intent(context, SendContactMsgAlarm.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
				SystemClock.elapsedRealtime() + INTERVAL,
				INTERVAL,
				pendingIntent
		);
	}
}
