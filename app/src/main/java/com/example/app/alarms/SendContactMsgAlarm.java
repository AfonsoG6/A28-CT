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
import com.example.app.OutgoingMsgManager;
import com.example.app.exceptions.DatabaseInsertionFailedException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;

public class SendContactMsgAlarm extends BroadcastReceiver {
	public static final long INTERVAL = AlarmManager.INTERVAL_FIFTEEN_MINUTES/15;

	ContactTracingService service;

	public SendContactMsgAlarm() { /* Empty */ }

	public SendContactMsgAlarm(ContactTracingService service) {
		this.service = service;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("SendContactMsgAlarm", "onReceive");
		Toast.makeText(context, "SendContactMsgAlarm", Toast.LENGTH_LONG).show();
		//TODO: Call service.sendContactMsg();
	}

	public void setAlarm(Context context) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());

		Intent intent = new Intent(context, SendContactMsgAlarm.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
				calendar.getTimeInMillis(),
				INTERVAL,
				pendingIntent
		);
	}
}
