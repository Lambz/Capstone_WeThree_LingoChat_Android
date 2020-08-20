package com.lambz.lingo_chat.models;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

public class UserLocation implements Serializable
{
    private double lat, lng;
    private String title;

    public UserLocation(double lat, double lng)
    {
        this.lat = lat;
        this.lng = lng;
    }

    public UserLocation(double lat, double lng, String title)
    {
        this.lat = lat;
        this.lng = lng;
        this.title = title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public double getLat()
    {
        return lat;
    }

    public double getLng()
    {
        return lng;
    }

    public String getTitle()
    {
        return title;
    }

    public LatLng getLatLng()
    {
        return new LatLng(lat,lng);
    }
}