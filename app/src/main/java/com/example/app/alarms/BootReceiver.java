package com.example.app.alarms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import com.example.app.ContactTracingService;
import com.example.app.helpers.SharedPrefsHelper;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("BootReceiver", "onReceive");
		Toast.makeText(context, "Boot Completed", Toast.LENGTH_LONG).show();
		if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
			SharedPrefsHelper spHelper = new SharedPrefsHelper(context);
			if (spHelper.hasPasswordSet()) {
				context.startForegroundService(new Intent(context, ContactTracingService.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
			}
		}
	}
}
