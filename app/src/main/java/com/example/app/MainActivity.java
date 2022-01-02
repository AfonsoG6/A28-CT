package com.example.app;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import com.example.hub.grpc.Hub.PingRequest;
import com.example.hub.grpc.Hub.PingResponse;
import com.example.hub.grpc.HubServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

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
			ManagedChannel channel = ManagedChannelBuilder.forAddress("10.0.2.2", 29292).usePlaintext().build();
			HubServiceGrpc.HubServiceBlockingStub stub = HubServiceGrpc.newBlockingStub(channel);
			PingRequest pingRequest = PingRequest.newBuilder().setContent(passwordET.getText().toString()).build();
			PingResponse pingResponse = stub.withDeadlineAfter(5, TimeUnit.SECONDS).ping(pingRequest);
			passwordET.setText(pingResponse.getContent());
			channel.shutdown();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}