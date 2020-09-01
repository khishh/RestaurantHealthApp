package com.example.cmpt276project.ui.restaurantList;

import com.google.maps.android.clustering.ClusterItem;


import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

public class ClusterItems implements ClusterItem {

    private LatLng mPosition;
    private String mTitle;
    private String mHazardLevel;
    private String mSnippet;
    private long id;


    public ClusterItems(double lat, double lng) {
        mPosition = new LatLng(lat, lng);
    }

    public ClusterItems(double lat, double lng, String title, String hazardLevel, String snippet, long id) {
        mPosition = new LatLng(lat, lng);
        mTitle = title;
        mHazardLevel = hazardLevel;
        mSnippet = snippet;
        this.id = id;
    }

    public String getmHazardLevel() {
        return mHazardLevel;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Nullable
    @Override
    public String getSnippet() {
        return mSnippet;
    }

    public long getId() {
        return id;
    }
}
