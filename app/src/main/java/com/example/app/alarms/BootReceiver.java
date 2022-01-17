package com.example.app.alarms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.example.app.ContactTracingService;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			Intent serviceIntent = new Intent(context, ContactTracingService.class);
			context.startService(serviceIntent);
		}
	}
}
