package com.miibarra.androidgpstracker;

import androidx.appcompat.app.AppCompatActivity;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

public class SavedLocationsDisplay extends AppCompatActivity {

    ListView lv_wayPointsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_locations_display);

        //Grab the list view so we can add the addresses to the activity
        lv_wayPointsList = findViewById(R.id.lv_wayPointsList);

        // Getting the list of saved locations
        GPSApplication gpsApplication = (GPSApplication) getApplicationContext();
        List<Location> savedLocations = gpsApplication.getLocationList();

        //This is a temporary hack to get more readable text to the waypoints list
        // TODO: Should create an address list in the GPSApplication class, call it in the
        // updateUIValues method when transforming lat/lon values to address so I can retrieve them here
        List<String> savedAddresses = new ArrayList<>(1);
        List<Address> addressList;
        Geocoder geocoder = new Geocoder(SavedLocationsDisplay.this);
        for(Location location : savedLocations){
            try {
                addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                savedAddresses.add(addressList.get(0).getAddressLine(0));
            }
            catch (Exception exception){
                savedAddresses.add("Unable to save address.");
            }
        }

        // Use array adapter to pass and display contents of list
        lv_wayPointsList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, savedAddresses));
    }
}