package com.example.cmpt276project.model;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.cmpt276project.model.dao.InspectionDao;
import com.example.cmpt276project.model.dao.RestaurantDao;
import com.example.cmpt276project.model.dao.ViolationDao;
import com.example.cmpt276project.util.Converters;

@Database( entities = {Restaurant.class, Inspection.class, Violation.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class MainDataBase extends RoomDatabase {

    private static MainDataBase instance;

    public static MainDataBase getInstance(Context context){

        if(instance == null){
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    MainDataBase.class,
                    "restaurant_database"
            ).build();
        }

        return instance;
    }

    public abstract RestaurantDao restaurantDao();

    public abstract InspectionDao inspectionDao();

    public abstract ViolationDao violationDao();
}
