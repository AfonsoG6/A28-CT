package com.example.app.activities;

import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.app.HubFrontend;
import com.example.app.R;
import com.example.app.activities.utils.ICCInputFilter;
import com.example.app.helpers.DatabaseHelper;
import com.example.hub.grpc.Hub;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.List;

public class ICCActivity extends AppCompatActivity {
	private static final String TAG = ICCActivity.class.getName();

	private TextView statusTextView;
	private EditText iccTextBox;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_icc);
		statusTextView = findViewById(R.id.statusTextView);
		iccTextBox = findViewById(R.id.iccEditText);
		InputFilter[] filters = new InputFilter[1];
		filters[0] = new ICCInputFilter();
		iccTextBox.setFilters(filters);
	}

	public void onSubmitInfectionClaim(View view) {
		statusTextView.setTextColor(getResources().getColor(R.color.white, getTheme()));
		statusTextView.setText("Sending Infection Claim...");
		statusTextView.setVisibility(View.VISIBLE);
		String icc = iccTextBox.getText().toString().trim().replace("-", "");
		List<Hub.SKEpochDayPair> sks;
		try (DatabaseHelper dbHelper = new DatabaseHelper(this)) {
			sks = dbHelper.getAllSKs();
		}
		try {
			HubFrontend frontend = HubFrontend.getInstance(getApplicationContext());
			frontend.claimInfection(false, icc, sks);
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