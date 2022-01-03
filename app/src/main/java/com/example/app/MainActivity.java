package com.example.app;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import com.example.hub.grpc.Hub.PingRequest;
import com.example.hub.grpc.Hub.PingResponse;
import com.example.hub.grpc.HubServiceGrpc;
import io.grpc.ManagedChannel;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	public void onPasswordSubmit(View view) {
		try {
			EditText passwordET = findViewById(R.id.passwordTextBox);
			HubFrontend frontend = new HubFrontend();
			String pingResponseString = frontend.ping(passwordET.getText().toString());
			passwordET.setText(pingResponseString);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}