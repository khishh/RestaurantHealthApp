package com.example.cmpt276project.ui.restaurantDetail;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.cmpt276project.model.Inspection;
import com.example.cmpt276project.model.database.dao.InspectionDao;
import com.example.cmpt276project.model.Restaurant;
import com.example.cmpt276project.model.database.dao.RestaurantDao;
import com.example.cmpt276project.model.database.dao.MainDataBase;

import java.util.List;

public class RestaurantDetailViewModel extends AndroidViewModel {

    private MutableLiveData<Restaurant> restaurant = new MutableLiveData<>();
    private MutableLiveData<List<Inspection>> inspectionList = new MutableLiveData<>();

    private FetchInspectionsFromDataBaseThread fromDataBaseThread;

    private RestaurantDao restaurantDao = MainDataBase.getInstance(getApplication()).restaurantDao();
    private InspectionDao inspectionDao = MainDataBase.getInstance(getApplication()).inspectionDao();

    public RestaurantDetailViewModel(@NonNull Application application) {
        super(application);
    }

    public void loadInspections(long restaurantId){
        fromDataBaseThread = new FetchInspectionsFromDataBaseThread(restaurantId);
        fromDataBaseThread.start();
    }

    private void fetchInspection(long restaurantId){
        Restaurant _restaurant = restaurantDao.getRestaurant(restaurantId);
        List<Inspection> _inspections = inspectionDao.getInspectionsOfOneRestaurant(restaurantId);

        restaurant.postValue(_restaurant);
        inspectionList.postValue(_inspections);
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
    }
}
