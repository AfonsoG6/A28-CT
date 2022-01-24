package com.example.app.activities;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.app.R;
import com.example.app.exceptions.PasswordCheckFailedException;
import com.example.app.helpers.DatabaseHelper;
import com.example.app.helpers.EpochHelper;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ShowContactsActivity extends AppCompatActivity {
	private static final String TAG = "ShowContactsActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_contacts);

		ListView contactsListView = findViewById(R.id.contactsListView);

		String password = getIntent().getStringExtra("password");
		List<DatabaseHelper.ContactInfo> contacts;
		try (DatabaseHelper dbHelper = new DatabaseHelper(this)) {
			contacts = dbHelper.getInfectedContacts(password);
		} catch (PasswordCheckFailedException | NoSuchAlgorithmException e) {
			e.printStackTrace();
			Toast.makeText(this, "Internal error occured", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		List<String> contactsStrings = new ArrayList<>(contacts.size());
		for (DatabaseHelper.ContactInfo contact : contacts) {
			String sb = EpochHelper.getDateFromIntervalN(contact.intervalN) + "\n" +
					getLocationString(contact.latitude, contact.longitude);
			contactsStrings.add(sb);
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, contactsStrings);
		contactsListView.setAdapter(adapter);
	}

	//Function that receives a latitude and longitude values and returns the corresponding address string
	private String getLocationString(double latitude, double longitude) {
		Geocoder geocoder = new Geocoder(this, Locale.ENGLISH);
		try {
			List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
			if (addresses == null || addresses.isEmpty()) {
				return "Unknown";
			}
			Address address = addresses.get(0);
			StringBuilder sb = new StringBuilder();

			String countryName = address.getCountryName();
			if (countryName == null || countryName.isEmpty()) countryName = "Unknown";
			sb.append(countryName).append(", ");

			String stateName = address.getAdminArea();
			if (stateName == null || stateName.isEmpty()) stateName = "Unknown";
			sb.append(stateName).append(", ");

			String localityName = address.getLocality();
			if (localityName == null || localityName.isEmpty()) localityName = "Unknown";
			sb.append(localityName).append(", ");

			String subLocalityName = address.getSubLocality();
			if (subLocalityName == null || subLocalityName.isEmpty()) subLocalityName = "Unknown";
			sb.append(subLocalityName).append(", ");

			String knownName = address.getFeatureName();
			if (knownName == null || knownName.isEmpty()) {
				knownName = address.getThoroughfare();
				if (knownName == null || knownName.isEmpty()) {
					knownName = "Unknown";
				}
			}
			sb.append(knownName);

			return (sb.toString().equals("Unknown, Unknown, Unknown, Unknown, Unknown")) ? "Unknown" : sb.toString();
		}
		catch (IOException e) {
			e.printStackTrace();
			return "Unknown";
		}
	}
}
