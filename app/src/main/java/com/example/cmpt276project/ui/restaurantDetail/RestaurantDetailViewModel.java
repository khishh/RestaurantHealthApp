package com.example.cmpt276project.ui.restaurantDetail;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.cmpt276project.model.Inspection;
import com.example.cmpt276project.model.database.dao.InspectionDao;
import com.example.cmpt276project.model.Restaurant;
import com.example.cmpt276project.model.database.dao.RestaurantDao;
import com.example.cmpt276project.model.database.dao.MainDataBase;

import java.util.List;

/**
 * RestaurantDetailViewModel
 *
 * 1. Fetch one restaurant and its inspections from database by its uuid
 * 2. Update isFavourite value of the displayed restaurant in database
 */

public class RestaurantDetailViewModel extends AndroidViewModel {

    private static final String TAG = RestaurantDetailViewModel.class.getSimpleName();

    private MutableLiveData<Restaurant> restaurant = new MutableLiveData<>();
    private MutableLiveData<List<Inspection>> inspectionList = new MutableLiveData<>();

    private FetchInspectionsFromDataBaseThread fromDataBaseThread;
    private UpdateIsFavouriteThread updateIsFavouriteThread;

    private RestaurantDao restaurantDao = MainDataBase.getInstance(getApplication()).restaurantDao();
    private InspectionDao inspectionDao = MainDataBase.getInstance(getApplication()).inspectionDao();

    public RestaurantDetailViewModel(@NonNull Application application) {
        super(application);
    }

    public void loadInspections(long restaurantId){
        fromDataBaseThread = new FetchInspectionsFromDataBaseThread(restaurantId);
        fromDataBaseThread.start();
    }

    public void updateIsFavouriteInDataBase(long restaurantId, boolean isFav){
        updateIsFavouriteThread = new UpdateIsFavouriteThread(restaurantId, isFav);
        updateIsFavouriteThread.start();
    }

    private void fetchInspection(long restaurantId){
        Restaurant _restaurant = restaurantDao.getRestaurant(restaurantId);
        List<Inspection> _inspections = inspectionDao.getInspectionsOfOneRestaurant(restaurantId);

        restaurant.postValue(_restaurant);
        inspectionList.postValue(_inspections);
    }

    private void updateIsFavourite(long restaurantId, boolean isFav){
        Restaurant target = restaurantDao.getRestaurant(restaurantId);
        target.setFav(!isFav);
        restaurantDao.updateRestaurant(target);
        // Test
        target = restaurantDao.getRestaurant(restaurantId);
        Log.e(TAG, target.toString());
    }

    private class FetchInspectionsFromDataBaseThread extends Thread{

        private long restaurantId;

        public FetchInspectionsFromDataBaseThread(long restaurantId){
            this.restaurantId = restaurantId;
        }

        @Override
        public void run() {
            fetchInspection(restaurantId);
        }
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

    public MutableLiveData<List<Inspection>> getInspectionList() {
        return inspectionList;
    }

    public MutableLiveData<Restaurant> getRestaurant() {
        return restaurant;
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        if(fromDataBaseThread != null){
            fromDataBaseThread.interrupt();
            fromDataBaseThread = null;
        }

        if(updateIsFavouriteThread != null){
            updateIsFavouriteThread.interrupt();
            updateIsFavouriteThread = null;
        }
    }
}
