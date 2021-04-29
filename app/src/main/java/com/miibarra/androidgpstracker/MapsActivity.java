package com.miibarra.androidgpstracker;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final float ZOOM_VALUE = 15;
    private GoogleMap mMap;
    List<Location> savedLocations;

    //NOTE: UNINSTALLING AND RUNNING APP AGAIN WITH API KEY WORKED IN SHOWING THE MAP
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        GPSApplication gpsApplication = (GPSApplication) getApplicationContext();
        savedLocations = gpsApplication.getLocationList();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        LatLng lastLocLatLon = sydney;

        // Loop through each location in saved locations and grab the lat/lon to display a pin on map
        for(Location location : savedLocations){
            LatLng locLatLon = new LatLng(location.getLatitude(), location.getLongitude());
            MarkerOptions marker = new MarkerOptions();
            marker.position(locLatLon);
            marker.title("Lat: " + location.getLatitude() + " Lon: " + location.getLongitude());
            mMap.addMarker(marker);

            lastLocLatLon = locLatLon;
        }

        //zoom in on last location
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastLocLatLon, ZOOM_VALUE));

        // Can do a bunch of different stuff with clicking the marker, but for now, show a toast on how many times its clicked
        // Can mess with different marker types in the future.
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                // Keep track of number of times that button is clicked
                Integer clicks  =  (Integer) marker.getTag();
                if(clicks == null){
                    clicks = 0;
                }
                clicks++;
                marker.setTag(clicks);
                Toast.makeText(MapsActivity.this,
                        "Marker "
                                + marker.getTitle()
                                + " was clicked for a total of "
                                + clicks
                                + " times!",
                        Toast.LENGTH_SHORT).show();

                return false;
            }
        });
    }
}