// Package
package com.example.cmpt276project.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Arrays;

// Violation Class
@Entity
public class Violation {

    /**
     * Enum
     */
    public enum Criticality {
        CRITICAL, NON_CRITICAL, ERROR
    }

    /**
     * Constants
     */
    public final static String[] CRITICALITY_KEYWORDS = {"critical", "not critical"};
    public final static String[] NATURE_KEYWORDS = {"equipment", "utensil", "food", "pest", "employee"};
    public final static String[] NATURE_KEYWORDS_CAP = {"Equipment", "Utensil", "Food", "Pest", "Employee"};
    private final static int NUM_OF_COMMAS = 3;

    /**
     * Fields
     */

    @NonNull
    @PrimaryKey (autoGenerate = true)
    private long violationId;
    @NonNull
    private long ownerInspectionId;
    private String description;
    private Criticality criticality;
    private int[] nature;
    private String longDescription;
    private String shortDescription;

    public Violation(){
    }

    @Ignore
    // Constructor
    public Violation(String description) {
        this.description = description;
        this.criticality = Criticality.ERROR; // Set the criticality as ERROR
        this.nature = new int[5];
        this.longDescription = "";

        violationAnalysis();
    }

    // Violation Analysis
    private void violationAnalysis() {
        int length = description.length();

        // Empty Description
        if (length == 0) {
            criticality = Criticality.ERROR; // Set the criticality as ERROR
            return;
        }

        // Find the index of comma
        int[] indexOfComma = new int[NUM_OF_COMMAS];
        int commaCount = 0;
        for (int i = 0; i < length; i++) {
            if (description.charAt(i) == ',') {
                commaCount++;

                // Error Case: # of commas > 3
                if (commaCount > NUM_OF_COMMAS) {
                    this.criticality = Criticality.ERROR; // Set the criticality as ERROR
                    return;
                }

                // Set the index of comma
                indexOfComma[commaCount - 1] = i;
            }
        }

        // Error Case: # of commas != 3
        if (commaCount != NUM_OF_COMMAS) {
            this.criticality = Criticality.ERROR; // Set the criticality as ERROR
            return;
        }

        // Call the methods to analyze
        // Find the Keyword Critical or Not Critical
        if(criticalityAnalysis(indexOfComma[0] + 1, indexOfComma[1] - 1)) {
            natureAnalysis(indexOfComma[1] + 1, indexOfComma[2] - 1);
            longDescriptionAnalysis(indexOfComma[1] + 1, indexOfComma[2] - 1);
            shortDescriptionAnalysis();
        }
    }

    // Copy the substring to longDescription
    private void longDescriptionAnalysis(int startIndex, int endIndex) {
        String substring = description.substring(startIndex, endIndex + 1);
        longDescription = substring;
    }

    // Violation Analysis
    // Determines the shortDescription
    private void shortDescriptionAnalysis() {

        boolean allDoesNotExist = true;
        String shortDescription = "";

        for (int i = 0; i < nature.length; i++) {
            if (nature[i] == 1) {
                if (!allDoesNotExist) {
                    shortDescription += "/";
                }

                shortDescription += NATURE_KEYWORDS_CAP[i];
                allDoesNotExist = false;
            }
        }

        if (allDoesNotExist)
            shortDescription = "Other";

        shortDescription += (" Issue");
        this.shortDescription = shortDescription;
    }

    // Determine the Criticality
    private boolean criticalityAnalysis(int startIndex, int endIndex) {
        String substring = (description.substring(startIndex, endIndex + 1)).toLowerCase();

        // NON_CRITICAL Case
        if (substring.equals(CRITICALITY_KEYWORDS[1])) {
            criticality = Criticality.NON_CRITICAL; // Set the criticality as NON_CRITICAL
        }
        // CRITICAL Case
        else if (substring.equals(CRITICALITY_KEYWORDS[0])) {
            criticality = Criticality.CRITICAL; // Set the criticality as CRITICAL
        }
        // ERROR Case
        else {
            criticality = Criticality.ERROR; // Set the criticality as ERROR
            return false;
        }
        return true;
    }

    // Determine the Nature
    private void natureAnalysis(int startIndex, int endIndex) {
        String substring = (description.substring(startIndex, endIndex + 1)).toLowerCase();

        // Contain equipment
        if (substring.contains(NATURE_KEYWORDS[0])) {
            nature[0] = 1;
        }
        // Contain utensil
        if (substring.contains(NATURE_KEYWORDS[1])) {
            nature[1] = 1;
        }
        // Contain food
        if (substring.contains(NATURE_KEYWORDS[2])) {
            nature[2] = 1;
        }
        // Contain pest
        if (substring.contains(NATURE_KEYWORDS[3])) {
            nature[3] = 1;
        }
        // Contain employee
        if (substring.contains(NATURE_KEYWORDS[4])) {
            nature[4] = 1;
        }
    }

    // Getters

    public long getViolationId() {
        return violationId;
    }

    public void setViolationId(long violationId) {
        this.violationId = violationId;
    }

    public long getOwnerInspectionId() {
        return ownerInspectionId;
    }

    public void setOwnerInspectionId(long ownerInspectionId) {
        this.ownerInspectionId = ownerInspectionId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Criticality getCriticality() {
        return criticality;
    }

    public void setCriticality(Criticality criticality) {
        this.criticality = criticality;
    }

    public int[] getNature() {
        return nature;
    }

    public void setNature(int[] nature) {
        this.nature = nature;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    @Override
    public String toString() {
        return "Violation{" +
                "violationId=" + violationId +
                ", ownerInspectionId=" + ownerInspectionId +
                ", description='" + description + '\'' +
                ", criticality=" + criticality +
                ", nature=" + Arrays.toString(nature) +
                ", longDescription='" + longDescription + '\'' +
                ", shortDescription='" + shortDescription + '\'' +
                '}';
    }
}