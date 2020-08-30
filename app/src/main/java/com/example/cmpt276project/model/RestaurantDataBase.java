package com.example.cmpt276project.model;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.cmpt276project.util.Converters;

@Database( entities = {Restaurant.class, Inspection.class, Violation.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class RestaurantDataBase extends RoomDatabase {

    private static RestaurantDataBase instance;

    public static RestaurantDataBase getInstance(Context context){

        if(instance == null){
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    RestaurantDataBase.class,
                    "restaurant_database"
            ).build();
        }

        return instance;
    }

    public abstract RestaurantDao restaurantDao();
}
