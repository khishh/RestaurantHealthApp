package com.example.cmpt276project.model.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.cmpt276project.model.Violation;

import java.util.List;

@Dao
public interface ViolationDao {

    @Insert
    public abstract List<Long> insertViolation(List<Violation> violations);

    @Query("SELECT * FROM Violation")
    public abstract List<Violation> getAllViolations();

    @Query("SELECT * FROM Violation WHERE ownerInspectionId == :ownerInspectionId")
    public abstract List<Violation> getViolationsOfOneInspection(long ownerInspectionId);

    @Query("DELETE FROM violation")
    public abstract void deleteAllViolation();
}
