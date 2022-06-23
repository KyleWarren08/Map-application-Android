package com.example.localguide.models;

import com.google.android.gms.maps.model.LatLng;

public class Favourite {

    private double lat;
    private double lng;
    private String  title;

    public Favourite() {

    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Favourite(double lat, double lng, String title) {
        this.lat = lat;
        this.lng = lng;
        this.title = title;
    }
}
