package com.example.android.rural;

public class Locations {
    private double longitude,latitude;
    private String name;

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getName() {
        return name;
    }

    public Locations(double lng,double lat,String names)
    {
        longitude=lng;
        latitude=lat;
        name=names;
    }
}
