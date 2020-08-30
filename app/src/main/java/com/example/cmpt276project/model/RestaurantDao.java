package com.example.cmpt276project.model;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface RestaurantDao {

    String TAG = RestaurantDao.class.getSimpleName();

    @Insert
    public abstract List<Long> insertRestaurant(List<Restaurant> restaurants);

    @Query("SELECT * FROM Restaurant ORDER BY name ASC")
    public abstract List<Restaurant> getAllRestaurants();

    @Query("SELECT * FROM Restaurant WHERE restaurantId == :restaurantId")
    public abstract Restaurant getRestaurant(long restaurantId);

    //    https://stackoverflow.com/questions/50198569/like-case-sensitive-in-room-persistence-library
    @Query("SELECT * FROM Restaurant WHERE name LIKE '%' || :query || '%' AND (totalNumIssues >= :min AND totalNumIssues <= :max) AND hazardLevelColor LIKE '%' ||  :hazardColor || '%' ORDER BY name ASC")
    public abstract List<Restaurant> getFilteredRestaurant(int min, int max, String query, String hazardColor);

    @Query("DELETE FROM Restaurant")
    public abstract void deleteAllRestaurant();

    @Insert
    public abstract List<Long> insertInspection(List<Inspection> inspections);

    @Query("SELECT * FROM Inspection")
    public abstract List<Inspection> getAllInspections();

    @Query("SELECT * FROM Inspection WHERE ownerRestaurantId == :ownerRestaurantId")
    public abstract List<Inspection> getInspectionsOfOneRestaurant(long ownerRestaurantId);

    @Query("SELECT * FROM Inspection WHERE inspectionId == :inspectionId")
    public abstract Inspection getAInspection(long inspectionId);

    @Query("DELETE FROM Inspection")
    public abstract void deleteAllInspection();

    @Insert
    public abstract List<Long> insertViolation(List<Violation> violations);

    @Query("SELECT * FROM Violation")
    public abstract List<Violation> getAllViolations();

    @Query("SELECT * FROM Violation WHERE ownerInspectionId == :ownerInspectionId")
    public abstract List<Violation> getViolationsOfOneInspection(long ownerInspectionId);

    @Query("DELETE FROM violation")
    public abstract void deleteAllViolation();
}
