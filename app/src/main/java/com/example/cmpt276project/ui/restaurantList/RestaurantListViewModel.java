package com.example.cmpt276project.ui.restaurantList;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.cmpt276project.model.Restaurant;
import com.example.cmpt276project.model.dao.RestaurantDao;
import com.example.cmpt276project.model.MainDataBase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

//    private FetchRestaurantDataFromDataBaseThread fetchRestaurantDataFromDataBaseThread;
    private FetchOneRestaurantByIdThread fetchOneRestaurantByIdThread;
    private FetchFilteredRestaurantThread fetchFilteredRestaurantThread;
    private FetchAllRestaurantNames fetchAllRestaurantNames;
    private UpdateIsFavouriteThread updateIsFavouriteThread;

    public RestaurantListViewModel(@NonNull Application application) {
        super(application);
    }

//    public void loadRestaurant(){
//        fetchRestaurantDataFromDataBaseThread = new FetchRestaurantDataFromDataBaseThread();
//        fetchRestaurantDataFromDataBaseThread.start();
//    }

//    private void fetchRestaurant(){
//        List<Restaurant> restaurants = dao.getAllRestaurants();
//        restaurantList.postValue(restaurants);
//    }

    public void fetchOneRestaurantById(long restaurantId) {
        fetchOneRestaurantByIdThread = new FetchOneRestaurantByIdThread(restaurantId);
        fetchOneRestaurantByIdThread.start();
    }

    private void fetchRestaurantById(long restaurantId) {
        Restaurant restaurant = dao.getRestaurant(restaurantId);
        restaurantLastVisited.postValue(restaurant);
    }

    public void fetchFilteredRestaurant(String query, String color, boolean isFavourite, int min, int max){
        fetchFilteredRestaurantThread = new FetchFilteredRestaurantThread(query, color, isFavourite, min, max);
        fetchFilteredRestaurantThread.start();
        fetchAllRestaurantNames = new FetchAllRestaurantNames();
        fetchAllRestaurantNames.start();
    }

    private void updateIsFavourite(long restaurantId, boolean isFav){
        Restaurant target = dao.getRestaurant(restaurantId);
        target.setFav(!isFav);
        dao.updateRestaurant(target);
        // Test
        target = dao.getRestaurant(restaurantId);
        Log.e(TAG, target.toString());
    }

    public void updateIsFavouriteInDataBase(long restaurantId, boolean isFav){
        updateIsFavouriteThread = new UpdateIsFavouriteThread(restaurantId, isFav);
        updateIsFavouriteThread.start();
    }

    private void fetchRestaurantByFilters(String query, String hazardLevelColor, boolean isFavourite, int min, int max){

        List<Restaurant> restaurant = new ArrayList<>();
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

    private class UpdateIsFavouriteThread extends Thread{
        long restaurantId;
        boolean isFav;

        public UpdateIsFavouriteThread(long restaurantId, boolean isFav){
            this.restaurantId = restaurantId;
            this.isFav = isFav;
        }

        @Override
        public void run() {
            updateIsFavourite(restaurantId, isFav);
        }
    }

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

//    private class FetchRestaurantDataFromDataBaseThread extends Thread{
//        @Override
//        public void run() {
//            fetchRestaurant();
//        }
//    }

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

    private void fetchRestaurantNames(){
        List<Restaurant> restaurants = dao.getAllRestaurants();
        HashMap<String, Long> nameAndIdMap = new HashMap<>();
        for(Restaurant restaurant : restaurants){
//            LatLng latLng = new LatLng(restaurant.getLatitude(), restaurant.getLongitude());
            nameAndIdMap.put(restaurant.getName(), restaurant.getRestaurantId());
        }
        restaurantNameIdMap.postValue(nameAndIdMap);
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

    public List<String> getAllRestaurantName(){
        List<String> restaurantNames = new ArrayList<>();
        for(Map.Entry<String, Long> e : Objects.requireNonNull(getRestaurantNameIdMap().getValue()).entrySet()){
            restaurantNames.add(e.getKey());
        }
        return restaurantNames;
    }

    @Override
    protected void onCleared() {
        super.onCleared();

//        if(fetchRestaurantDataFromDataBaseThread != null){
//            fetchRestaurantDataFromDataBaseThread.interrupt();
//            fetchRestaurantDataFromDataBaseThread = null;
//        }

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
