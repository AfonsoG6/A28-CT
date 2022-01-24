package com.example.app;

import android.app.*;
import android.content.Intent;
import android.graphics.Color;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.Nullable;
import com.example.app.activities.MainActivity;
import com.example.app.alarms.QueryInfectedSKsAlarm;
import com.example.app.alarms.SendContactMsgAlarm;
import com.example.app.alarms.SendDummyICCMsgAlarm;
import com.example.app.bluetooth.ContactServer;
import com.example.app.exceptions.DatabaseInsertionFailedException;
import lombok.Getter;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class ContactTracingService extends Service {
	private static final String TAG = ContactTracingService.class.getName();

	@Getter private OutgoingMsgManager outMsgManager;
	@Getter private IncomingMsgManager inMsgManager;

	private QueryInfectedSKsAlarm queryInfectedSKsAlarm;
	private SendContactMsgAlarm sendContactMsgAlarm;
	private SendDummyICCMsgAlarm sendDummyICCMsgAlarm;

	@Override
	public void onCreate() {
		super.onCreate();
		try {
			inMsgManager = new IncomingMsgManager(getApplicationContext());
			ContactServer.setInMsgManager(inMsgManager);
			ContactServer.startServer(this);
			outMsgManager = new OutgoingMsgManager(getApplicationContext());
		}
		catch (NoSuchAlgorithmException | DatabaseInsertionFailedException | IOException e) {
			e.printStackTrace();
		}
		queryInfectedSKsAlarm = new QueryInfectedSKsAlarm(this);
		sendContactMsgAlarm = new SendContactMsgAlarm(this);
		sendDummyICCMsgAlarm = new SendDummyICCMsgAlarm(this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		setAlarms();
		startForeground(29, setupNotification());
		Log.i("ContactTracingService", "onStartCommand");
		Toast.makeText(this, "CT Service Started", Toast.LENGTH_LONG).show();
		return START_STICKY;
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private void setAlarms() {
		queryInfectedSKsAlarm.setAlarm(this);
		sendContactMsgAlarm.setAlarm(this);
		sendDummyICCMsgAlarm.setAlarm(this);
	}

	public boolean getInfectedStatus() {
		return inMsgManager.queryInfectedSks();
	}

	private Notification setupNotification() {
		Intent notificationIntent = new Intent(this, MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		NotificationChannel channel = new NotificationChannel("ct_service", getText(R.string.ct_service_title), NotificationManager.IMPORTANCE_DEFAULT);
		channel.setDescription(getText(R.string.ct_service_text).toString());
		channel.enableLights(true);
		channel.setLightColor(Color.BLUE);
		NotificationManager notificationManager = getSystemService(NotificationManager.class);
		notificationManager.createNotificationChannel(channel);

		return new Notification.Builder(this, channel.getId())
				.setOngoing(true)
				.setContentTitle(getText(R.string.ct_service_title))
				.setContentText(getText(R.string.ct_service_text))
				.setContentIntent(pendingIntent)
				.setTicker(getText(R.string.ct_service_ticker))
				.build();
	}

	public void sendDummyICCMsg() {
		try {
			HubFrontend hubFrontend = HubFrontend.getInstance(this);
			hubFrontend.sendDummyClaimInfection();
		}
		catch (NoSuchAlgorithmException | CertificateException | KeyStoreException | IOException | KeyManagementException e) {
			e.printStackTrace();
		}
	}

	public void sendContactMsg() {
		try {
			outMsgManager.sendContactMsg(this);
		} catch (DatabaseInsertionFailedException | NoSuchAlgorithmException | IOException e) {
			e.printStackTrace();
		}
	}

	public void queryInfectedSks() {
		inMsgManager.queryInfectedSks();
	}
}
