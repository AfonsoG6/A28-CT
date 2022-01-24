package com.example.app.activities;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import com.example.app.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class ShowContactsActivity extends AppCompatActivity {
	private static final String TAG = "ShowContactsActivity";

	private RecyclerView contactListRecyclerView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_contacts);

		contactListRecyclerView = findViewById(R.id.contactListRecyclerView);
		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(contactListRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
		contactListRecyclerView.addItemDecoration(dividerItemDecoration);

		//TODO: Implement the code to show the contacts in the RecyclerView
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
