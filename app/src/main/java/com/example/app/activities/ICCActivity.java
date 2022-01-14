package com.example.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.app.HubFrontend;
import com.example.app.R;
import com.example.hub.grpc.Hub;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

public class ICCActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_icc);
	}

	public void onSubmitInfectionClaim(View view) {
		TextView statusTextView = findViewById(R.id.statusTextView);
		statusTextView.setTextColor(getResources().getColor(R.color.white, getTheme()));
		statusTextView.setText("Sending Infection Claim...");
		statusTextView.setVisibility(View.VISIBLE);
		// TODO: Dont Allow invalid characters in iccTextBox (probably not in this function tho)
		EditText iccTextBox = findViewById(R.id.iccEditText);
		List<Hub.SKEpochDayPair> sksTEMPORARY = new ArrayList<>(); // FIXME: TEMPORARY
		try {
			HubFrontend frontend = HubFrontend.getInstance(getApplicationContext());
			frontend.claimInfection(false, iccTextBox.getText().toString(), sksTEMPORARY);
		}
		catch (StatusRuntimeException e) {
			statusTextView.setTextColor(getResources().getColor(R.color.red, getTheme()));
			if (e.getStatus().getCode() == Status.INVALID_ARGUMENT.getCode()) {
				statusTextView.setText("Invalid Infection Claim Code!");
			}
			else {
				statusTextView.setText("Something went wrong...");
			}
		}
		catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | IOException | KeyManagementException e) {
			statusTextView.setText("Something went incredibly wrong...");
		}
	}
}