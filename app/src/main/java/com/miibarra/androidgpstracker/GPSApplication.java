package com.miibarra.androidgpstracker;

import android.app.Application;
import android.location.Location;

import java.util.ArrayList;
import java.util.List;

public class GPSApplication extends Application {

    public static GPSApplication singleton;

    private List<Location> locationList;

    public List<Location> getLocationList() {
        return locationList;
    }

    public void setLocationList(List<Location> locationList) {
        this.locationList = locationList;
    }

    public GPSApplication getInstance(){
        return singleton;
    }

    public void onCreate(){
        super.onCreate();
        singleton = this;
        locationList = new ArrayList<>();
    }
}
