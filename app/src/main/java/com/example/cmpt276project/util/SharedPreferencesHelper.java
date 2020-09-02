package com.example.cmpt276project.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


public class SharedPreferencesHelper {

    private static final String LAST_LAUNCH_TIME = "last_launch_time";
    private static final String LAST_MODIFIED_DATE_RESTAURANT = "restaurant_update";
    private static final String LAST_MODIFIED_DATE_INSPECTION = "inspection_update";
    private static final String LAST_VISITED_RESTAURANT = "last_visited_restaurant";
    private static final String RESTAURANT_ACTIVITY_STATE = "restaurant_activity_state";

    private SharedPreferences preferences;
    private static SharedPreferencesHelper sharedPreferencesHelper;


    private SharedPreferencesHelper(Context context){
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static SharedPreferencesHelper getInstance(Context context){
        if(sharedPreferencesHelper == null){
            sharedPreferencesHelper = new SharedPreferencesHelper(context);
        }
        return sharedPreferencesHelper;
    }

    public void saveUpdateTime(long time){
        preferences.edit().putLong(LAST_LAUNCH_TIME, time).apply();
    }

    public long getLastLaunchTIme(){
        return preferences.getLong(LAST_LAUNCH_TIME, 0);
    }

    public void saveLastLaunchTime(String newModifiedDate){
        preferences.edit().putString(LAST_MODIFIED_DATE_RESTAURANT, newModifiedDate).apply();
    }

    public String getLastModifiedDateRestaurant() {
        return preferences.getString(LAST_MODIFIED_DATE_RESTAURANT, "");
    }

    public void saveLastModifiedDateInspection(String newModifiedDate){
        preferences.edit().putString(LAST_MODIFIED_DATE_INSPECTION, newModifiedDate).apply();
    }

    public String getLastModifiedDateInspection() {
        return preferences.getString(LAST_MODIFIED_DATE_INSPECTION, "");
    }

    public void saveLastVisitedRestaurant(long restaurantId){
        preferences.edit().putLong(LAST_VISITED_RESTAURANT, restaurantId).apply();
    }

    public long getLastVisitedRestaurant(){
        return preferences.getLong(LAST_VISITED_RESTAURANT, 0);
    }

    public void saveFilters(String query, String queryColor, Boolean isFav, int queryMin, int queryMax) {
        preferences.edit().putString("SEARCH_BAR", query)
                .putString("COLOR_SELECT", queryColor)
                .putBoolean("FAV_SELECT", isFav)
                .putInt("MIN_SELECT", queryMin)
                .putInt("MAX_SELECT", queryMax)
                .apply();
    }

    public String getQuery() {
        return preferences.getString("SEARCH_BAR", "");
    }

    public String getQueryColor() {
        return preferences.getString("COLOR_SELECT", "All");
    }

    public Boolean getFav() {
        return preferences.getBoolean("FAV_SELECT", false);
    }

    public Integer getQueryMin() {
        return preferences.getInt("MIN_SELECT", 0);
    }

    public Integer getQueryMax() {
        return preferences.getInt("MAX_SELECT", 100);
    }

    public void saveRestaurantListActivityState(boolean isMapShown){
        preferences.edit().putBoolean(RESTAURANT_ACTIVITY_STATE, isMapShown).apply();
    }

    public boolean getRestaurantListActivityState(){
        return preferences.getBoolean(RESTAURANT_ACTIVITY_STATE, true);
    }

}
