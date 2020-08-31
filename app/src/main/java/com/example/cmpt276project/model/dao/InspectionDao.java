package com.example.cmpt276project.model.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.cmpt276project.model.Inspection;

import java.util.List;

@Dao
public interface InspectionDao {

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

}
