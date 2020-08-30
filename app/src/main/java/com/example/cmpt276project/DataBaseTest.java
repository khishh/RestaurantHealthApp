package com.example.cmpt276project;

import android.content.Context;
import android.util.Log;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.cmpt276project.model.Restaurant;
import com.example.cmpt276project.model.RestaurantDao;
import com.example.cmpt276project.model.RestaurantDataBase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class DataBaseTest {

//    private static final String TAG = DataBaseTest.class.getSimpleName();
//    private RestaurantDao dao;
//    private RestaurantDataBase dataBase;

//    @Before
//    public void createDb(){
//        Context context = ApplicationProvider.getApplicationContext();
//        dataBase = Room.inMemoryDatabaseBuilder(context, RestaurantDataBase.class). build();
//        dao = dataBase.restaurantDao();
//    }
//
//    @After
//    public void closeDb() throws IOException {
//        dataBase.close();
//    }
//
//
    @Test
    public void writeRestaurantInDataBaseTest(){
//
//        List<Restaurant> insertTest = new ArrayList<>();
//        Restaurant restaurant = new Restaurant(
//                "SDFO-8HKP7E",
//                "Pattullo A&W",
//                "12808 King George Blvd",
//                49.20610961,
//                -122.8668064
//        );
//
//        insertTest.add(restaurant);
//
//        dao.insertRestaurant(insertTest);
//
//        List<Restaurant> res = dao.getAllRestaurants();
//        Log.d(TAG, res.toString());
//
    }

}
