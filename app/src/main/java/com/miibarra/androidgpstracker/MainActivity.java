package com.miibarra.androidgpstracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final long DEFAULT_UPDATE_INTERVAL = 30000;
    private static final long FAST_UPDATE_INTERVAL = 5000;
    private static final int PERMISSION_FINE_LOCATION = 1;

    // Adding UI elements to main
    TextView tv_lat;
    TextView tv_lon;
    TextView tv_altitude;
    TextView tv_accuracy;
    TextView tv_speed;
    TextView tv_sensor;
    TextView tv_updates;
    TextView tv_address;
    TextView tv_labelWaypointsCount;

    Switch sw_locationUpdates;
    Switch sw_gps;

    Button btn_newWayPoint;
    Button btn_showWayPointList;
    Button btn_showGoogleMap;

    // Save off the current location
    Location currentLocation;
    // Keep list of saved locations
    List<Location> savedLocations;

    // Google's API for location services. The majority of the app functions use this classs
    FusedLocationProviderClient fusedLocationProviderClient;
    boolean updateOn = false; // Keep track of whether we are tracking location or not

    // location request is a config file for all settings related to FusedLocationProviderClient
    LocationRequest locationRequest;
    // location callback is used to receive notifications from FusedLocationProvider when location
    // is changed are can't be found
    LocationCallback locationCallBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setting all ui components
        tv_lat = findViewById(R.id.tv_lat);
        tv_lon = findViewById(R.id.tv_lon);
        tv_altitude = findViewById(R.id.tv_altitude);
        tv_accuracy = findViewById(R.id.tv_accuracy);
        tv_speed = findViewById(R.id.tv_speed);
        tv_sensor = findViewById(R.id.tv_sensor);
        tv_updates = findViewById(R.id.tv_updates);
        tv_address = findViewById(R.id.tv_address);
        sw_locationUpdates = findViewById(R.id.sw_locationsupdates);
        sw_gps = findViewById(R.id.sw_gps);
        tv_labelWaypointsCount = findViewById(R.id.tv_labelWaypointsCount);
        btn_newWayPoint = findViewById(R.id.btn_newWayPoint);
        btn_showWayPointList = findViewById(R.id.btn_showWayPointList);
        btn_showGoogleMap = findViewById(R.id.btn_showGoogleMaps);

        // Set all properties of LocationRequest
        locationRequest = LocationRequest.create();
        // How often does the default location check occur
        locationRequest.setInterval(DEFAULT_UPDATE_INTERVAL);
        // How often does the location check occur when set to the most request update
        locationRequest.setFastestInterval(FAST_UPDATE_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        //
        locationCallBack = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                // Need to send last known location to UI and getLastLocation returns the location
                // object needed
                updateUIValues(locationResult.getLastLocation());
            }
        };

        sw_gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sw_gps.isChecked()) {
                    //If checked then we will use most accurate location - GPS
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    tv_sensor.setText("Using GPS Sensor");
                } else {
                    // Otherwise use our default setting
                    locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                    tv_sensor.setText("Using Cell Towers and WIFI");
                }
            }
        });

        sw_locationUpdates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //<code>true</code> to turn on location tracking
                //<code>false</code> to turn off
                setLocationTracking(sw_locationUpdates.isChecked());
            }
        });

        btn_newWayPoint.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // Need to get GPS location and add it to the global list

                // Then we need to add the location to global list of locations
                GPSApplication gpsApplication = (GPSApplication) getApplicationContext();
                savedLocations = gpsApplication.getLocationList();
                savedLocations.add(currentLocation);
            }
        });

        btn_showWayPointList.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SavedLocationsDisplay.class);
                startActivity(intent);
            }
        });

        //NOTE: UNINSTALLING AND RUNNING APP AGAIN WITH API KEY WORKED IN SHOWING THE MAP
        btn_showGoogleMap.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });

        updateGPS();
    }

    private void setLocationTracking(boolean trackLocation) {
        if (trackLocation) {
            tv_updates.setText("Location tracking: ON");
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null);
            }
        }
        else{
            tv_updates.setText("Location tracking: OFF");
            tv_lat.setText("N/A");
            tv_lon.setText("N/A");
            tv_speed.setText("N/A");
            tv_address.setText("N/A");
            tv_accuracy.setText("N/A");
            tv_altitude.setText("N/A");
            tv_sensor.setText("N/A");

            fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode){
            case PERMISSION_FINE_LOCATION:
                // If permission matches then we can update GPS
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    updateGPS();
                }
                // Otherwise notify user that permission is required and close app
                else{
                    Toast.makeText(this, "Please allow location permission to use app functionality.", Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    /**
     * This method will get permissions from the user to track with GPS with
     * current location from fused client to update UI
     */
    private void updateGPS(){
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);

        // <code>true</code> if we have permission to use GPS
        // <code>false</code> permission not granted so need to prompt user to use GPS
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED){
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    // Since we have permission update UI components with location values
                    updateUIValues(location);
                    // Current location at this time
                    currentLocation = location;
                }
            });
        }
        else{
            // Need to check if current Android OS value is high enough (23 or higher)
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                // This takes in the permissions as String[] and request code to match with result >= 0
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_LOCATION);
            }

        }
    }

    /**
     * This will update all the textview objects with a new location
     */
    private void updateUIValues(Location location) {
        tv_lat.setText(String.valueOf(location.getLatitude()));
        tv_lon.setText(String.valueOf(location.getLongitude()));
        tv_accuracy.setText(String.valueOf(location.getAccuracy()));

        //check if location has altitude value to set text view
        if(location.hasAltitude()){
            tv_altitude.setText(String.valueOf(location.getAltitude()));
        }
        else{
            tv_altitude.setText("Data not available.");
        }

        if(location.hasSpeed()){
            tv_speed.setText(String.valueOf(location.getSpeed()));
        }
        else{
            tv_speed.setText("Data not available.");
        }

        // This will be used to take a lat/lon and transform it into an address or partial address
        // and vice-versa
        Geocoder geocoder = new Geocoder(MainActivity.this);
        try{
            List<Address> addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            tv_address.setText(addressList.get(0).getAddressLine(0));
        }
        catch(Exception exception){
            tv_address.setText("Unable to read address.");
        }

        GPSApplication gpsApplication = (GPSApplication) getApplicationContext();
        savedLocations = gpsApplication.getLocationList();
        // Need to update number of waypoints with count of locations in list
        // TODO: Have to figure out how to get this to update when a new waypoint is added and not after the
        // location tracking is toggled on/off
        tv_labelWaypointsCount.setText(Integer.toString(savedLocations.size()));
    }
}