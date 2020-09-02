package com.example.cmpt276project.ui.restaurantList;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.cmpt276project.model.Restaurant;
import com.example.cmpt276project.model.database.dao.RestaurantDao;
import com.example.cmpt276project.model.database.dao.MainDataBase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * RestaurantListViewModel
 *
 * 1. fetch one restaurant by its own uuid (= restaurantId)
 * 2. fetch all restaurant meeting filtering requirements
 * 3. retrieve all restaurant names stored in database (used for autosuggestion)
 *
 */

public class RestaurantListViewModel extends AndroidViewModel {

    private static final String TAG = RestaurantListViewModel.class.getSimpleName();
    private static final String DEFAULT_QUERY = "";
    private static final String DEFAULT_COLOR = "All";
    private static final boolean DEFAULT_IS_FAV = false;
    private static final int DEFAULT_MIN = 0;
    private static final int DEFAULT_MAX = 100;

    private MutableLiveData<List<Restaurant>> restaurantList = new MutableLiveData<>();
    private MutableLiveData<Restaurant> restaurantLastVisited = new MutableLiveData<>();
    private MutableLiveData<HashMap<String, Long>> restaurantNameIdMap = new MutableLiveData<>();

    private RestaurantDao dao = MainDataBase.getInstance(getApplication()).restaurantDao();

    private FetchOneRestaurantByIdThread fetchOneRestaurantByIdThread;
    private FetchFilteredRestaurantThread fetchFilteredRestaurantThread;
    private FetchAllRestaurantNames fetchAllRestaurantNames;

    public RestaurantListViewModel(@NonNull Application application) {
        super(application);
    }

    public void fetchOneRestaurantById(long restaurantId) {
        fetchOneRestaurantByIdThread = new FetchOneRestaurantByIdThread(restaurantId);
        fetchOneRestaurantByIdThread.start();
    }

    public void fetchFilteredRestaurant(String query, String color, boolean isFavourite, int min, int max){
        fetchFilteredRestaurantThread = new FetchFilteredRestaurantThread(query, color, isFavourite, min, max);
        fetchFilteredRestaurantThread.start();
        fetchAllRestaurantNames = new FetchAllRestaurantNames();
        fetchAllRestaurantNames.start();
    }

    public List<String> getAllRestaurantName(){
        List<String> restaurantNames = new ArrayList<>();
        for(Map.Entry<String, Long> e : Objects.requireNonNull(getRestaurantNameIdMap().getValue()).entrySet()){
            restaurantNames.add(e.getKey());
        }
        return restaurantNames;
    }

    private void fetchRestaurantById(long restaurantId) {
        Restaurant restaurant = dao.getRestaurant(restaurantId);
        restaurantLastVisited.postValue(restaurant);
    }

    private void fetchRestaurantByFilters(String query, String hazardLevelColor, boolean isFavourite, int min, int max){

        List<Restaurant> restaurant;
        // default filter values
        if(query.equals(DEFAULT_QUERY) && isFavourite == DEFAULT_IS_FAV && min == DEFAULT_MIN && max == DEFAULT_MAX && hazardLevelColor.equals(DEFAULT_COLOR)){
            restaurant = dao.getAllRestaurants();
        }
        else{
            if(hazardLevelColor.equals(DEFAULT_COLOR)){
                hazardLevelColor = "";
            }
            restaurant = dao.getFilteredRestaurant(min, max, query, hazardLevelColor, isFavourite);
        }

        restaurantList.postValue(restaurant);
    }

    private void fetchRestaurantNames(){
        List<Restaurant> restaurants = dao.getAllRestaurants();
        HashMap<String, Long> nameAndIdMap = new HashMap<>();
        for(Restaurant restaurant : restaurants){
            nameAndIdMap.put(restaurant.getName(), restaurant.getRestaurantId());
        }
        restaurantNameIdMap.postValue(nameAndIdMap);
    }

    /**
     * Thread classes
     */

    private class FetchOneRestaurantByIdThread extends Thread{

        long restaurantId;

        public FetchOneRestaurantByIdThread(long id){
            this.restaurantId = id;
        }

        @Override
        public void run() {
            fetchRestaurantById(restaurantId);
        }
    }

    private class FetchFilteredRestaurantThread extends Thread{

        String query, color;
        boolean isFav;
        int min, max;

        public FetchFilteredRestaurantThread(String query, String color, boolean isFav, int min, int max){
            this.query = query;
            this.color = color;
            this.isFav = isFav;
            this.min = min;
            this.max = max;
        }

        @Override
        public void run() {
            fetchRestaurantByFilters(query, color, isFav, min, max);
        }
    }

    private class FetchAllRestaurantNames extends Thread{
        @Override
        public void run() {
            fetchRestaurantNames();
        }
    }


    public MutableLiveData<List<Restaurant>> getRestaurantList() {
        return restaurantList;
    }

    public long getRestaurantIdByName(String name){
        return restaurantNameIdMap.getValue().get(name);
    }

    public MutableLiveData<Restaurant> getRestaurantLastVisited() {
        return restaurantLastVisited;
    }

    public MutableLiveData<HashMap<String, Long>> getRestaurantNameIdMap() {
        return restaurantNameIdMap;
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        if(fetchOneRestaurantByIdThread != null){
            fetchOneRestaurantByIdThread.interrupt();
            fetchOneRestaurantByIdThread = null;
        }

        if(fetchFilteredRestaurantThread != null){
            fetchFilteredRestaurantThread.interrupt();
            fetchFilteredRestaurantThread = null;
        }

        if(fetchAllRestaurantNames != null){
            fetchAllRestaurantNames.interrupt();
            fetchAllRestaurantNames = null;
        }

    }
}
