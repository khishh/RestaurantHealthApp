/*
    This is the Inspection class implementation.
    Last Modified Date: 2020/07/08

    This class requires 7 inputs
    1. String trackingNumber
    2. String inspectionDate_S
    3. String inspectionType_S
    4. String numOfCritical_S
    5. String numOfNonCritical_S
    6. String hazardLevel
    7. String violations_S

    Remark:
    1. The Variable violations requires violationsAnalysis method to separate different violations from a string
    2. Input (2 Critical Numbers) should be same as the sum of critical and non-critical violations after calculate
 */

// Package
package com.example.cmpt276project.model;

// Import
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

// Inspection Class
@Entity
public class Inspection {

    @NonNull
    @PrimaryKey(autoGenerate = true)
    private long inspectionId;

    @NonNull
    private long ownerRestaurantId;

    // Enum
    public enum InspectionType {
        ROUTINE, FOLLOW_UP
    }
    public enum HazardLevel {
        LOW, MODERATE, HIGH
    }

    // Constant
    private static final String TAG = Inspection.class.getSimpleName();
    public static final String[] INSPECTION_TYPE_S = {"Routine", "Follow-Up"};
    public static final String[] HAZARD_LEVEL_S = {"Low", "Moderate", "High"};
    private static final String[] MONTH = {"January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"};
    private static final String dateFormat = "yyyyMMdd";

    // Restaurant Number
    private String trackingNumber;

    // Inspection Date and Type
    private Date inspectionDate;
    private String formattedDate;
    private InspectionType inspectionType;

    // Violation Details
    private int numOfCritical;
    private int numOfNonCritical;
    private HazardLevel hazardLevel;

    @Ignore
    private ArrayList<Violation> violations;

    public Inspection(){
    }

    @Ignore
    // Constructor
    public Inspection(String trackingNumber, String inspectionDate_S, String inspectionType_S, String numOfCritical_S,
                      String numOfNonCritical_S, String hazardLevel_S, String violations_S) {
        this.trackingNumber = trackingNumber;
        initializeDate(inspectionDate_S);
        initializeType(inspectionType_S);
        initializeCritical(numOfCritical_S, numOfNonCritical_S);
        initializeHazardLevel(hazardLevel_S);
        this.violations = new ArrayList<>();

        violationsAnalysis(violations_S);
    }


    private void initializeDate(String inspectionDate_S) {
        int length = inspectionDate_S.length();

        // Error: Invalid Input - Date Format
        if (length != 8) {
//            errorInvalidInputDate();
            return;
        }

        SimpleDateFormat ft = new SimpleDateFormat(dateFormat, Locale.CANADA);

        try{
            inspectionDate = ft.parse(inspectionDate_S);
        } catch (ParseException e) {
            Log.e(TAG, "Date format error");
        }

        if(inspectionDate == null){
            formattedDate = "No Inspection Date Available";
        }
        else{
            long timeDiff = System.currentTimeMillis() - inspectionDate.getTime();
            int daysDiff = (int) (timeDiff / (24*60*60*1000));
//            Log.e(TAG, "time Diff == " + timeDiff + " dat diff == " + daysDiff);

            Calendar calendar = Calendar.getInstance(Locale.CANADA);
            calendar.setTime(inspectionDate);
            DateFormatSymbols dfs = new DateFormatSymbols();
            String[] month = dfs.getMonths();

            // case if timeDiff is within 30 days
            if(daysDiff <= 30){
                formattedDate = String.format(Locale.getDefault(),
                        "%d days ago",
                        daysDiff
                );
            }
            else if(daysDiff <= 365){
                formattedDate = String.format(Locale.getDefault(),
                        "%s %d",
                        month[calendar.get(Calendar.MONTH)],
                        calendar.get(Calendar.DAY_OF_MONTH)
                        );
            }
            else{
                formattedDate = String.format(Locale.getDefault(),
                        "%s %d, %d",
                        month[calendar.get(Calendar.MONTH)],
                        calendar.get(Calendar.DAY_OF_MONTH),
                        calendar.get(Calendar.YEAR)
                );
            }
        }

//        Log.e(TAG, "Orig == " + inspectionDate_S + " formattedDate == " + formattedDate);
    }

//    private void errorInvalidInputDate() {
//        this.inspectionYear = 1900;
//        this.inspectionMonth = 1;
//        this.inspectionDay = 1;
//        Log.e(TAG, "Error: Invalid Input - Date Format");
//    }

    private void initializeType(String inspectionType_S) {
        // Routine Case
        if (inspectionType_S.equals(INSPECTION_TYPE_S[0])) {
            this.inspectionType = InspectionType.ROUTINE;
        }
        // Follow-Up Case
        else if (inspectionType_S.equals(INSPECTION_TYPE_S[1])) {
            this.inspectionType = InspectionType.FOLLOW_UP;
        }
        // Error Case
        else {
            this.inspectionType = InspectionType.ROUTINE;
            Log.e(TAG, "Error: Invalid Input - Inspection Type ==> " + inspectionType_S + " tracking number == " + trackingNumber);
        }
    }

    private void initializeCritical(String numOfCritical_S, String numOfNonCritical_S) {
        // Number of Critical Issues

        int numOfCritical = 0;
        try {
            numOfCritical = Integer.parseInt(numOfCritical_S);
        }
        catch (Exception e) {
            errorInvalidInputCritical();
            Log.e(TAG, "Error: Invalid Input - numOfCritical ==> " + numOfCritical_S + " tracking number == " + trackingNumber);
            return;
        }
        if (numOfCritical >= 0) {
            this.numOfCritical = numOfCritical;
        }
        else {
            Log.e(TAG, "Error: Invalid Input - numOfCritical ==> " + numOfCritical_S + " tracking number == " + trackingNumber);
            errorInvalidInputCritical();
            return;
        }

        // Number of Non-Critical Issues
        int numOfNonCritical = 0;
        try {
            numOfNonCritical = Integer.parseInt(numOfNonCritical_S);
        }
        catch (Exception e) {
            Log.e(TAG, "Error: Invalid Input - numOfNonCritical ==> " + numOfNonCritical_S + " tracking number == " + trackingNumber);
            errorInvalidInputCritical();
            return;
        }
        if (numOfNonCritical >= 0) {
            this.numOfNonCritical = numOfNonCritical;
        }
        else {
            Log.e(TAG, "Error: Invalid Input - numOfNonCritical ==> " + numOfNonCritical_S + " tracking number == " + trackingNumber);
            errorInvalidInputCritical();
            return;
        }
    }

    private void errorInvalidInputCritical() {
        this.numOfCritical = 0;
        this.numOfNonCritical = 0;
        Log.e(TAG, "Error: Invalid Input - Critical or Non-Critical Number");
    }

    private void initializeHazardLevel(String hazardLevelString) {
        // Set the Hazard Level
        if (hazardLevelString.equals(HAZARD_LEVEL_S[0])) {
            this.hazardLevel = HazardLevel.LOW;
        }
        else if (hazardLevelString.equals(HAZARD_LEVEL_S[1])) {
            this.hazardLevel = HazardLevel.MODERATE;
        }
        else if (hazardLevelString.equals(HAZARD_LEVEL_S[2])) {
            this.hazardLevel = HazardLevel.HIGH;
        }
        else {
            this.hazardLevel = HazardLevel.LOW;
            Log.e(TAG, "Error: Invalid Input - Hazard Level");
        }
    }

    // Separate different violations from a string
    private void violationsAnalysis(String violationsString) {
        int length = violationsString.length();

        // Empty String
        if (length == 0) {
            // Check Number of Critical / Non-Critical
            checkNumOfCritical();
            return;
        }

        // Loop for separating violations from a string
        int startIndex = 0;
        for (int i = 0; i < length; i++) {
            if (violationsString.charAt(i) == '|') {
                String substring = violationsString.substring(startIndex, i);
                Violation violation = new Violation(substring);

                // If there is an error in this violation, then does not add to the ArrayList
                if (violation.getCriticality() != Violation.Criticality.ERROR) {
                    violations.add(violation);
                }
                startIndex = i + 1;
            }
        }

        // Only one violation or the Last one
        String substring = violationsString.substring(startIndex, length);
        Violation violation = new Violation(substring);

        // If there is an error in this violation, then does not add to the ArrayList
        if (violation.getCriticality() != Violation.Criticality.ERROR) {
            violations.add(violation);
        }

        // Check Number of Critical / Non-Critical
//        checkNumOfCritical();
    }

    private void checkNumOfCritical() {

        int numOfIssues = violations.size();

        // Empty violation
        if (numOfIssues == 0) {
            if (numOfCritical != 0 || numOfNonCritical != 0) {
                Log.e(TAG, "The Number of Critical or Number of NonCritical is not equal to zero");
                this.numOfCritical = 0;
                this.numOfNonCritical = 0;
            }
            return;
        }

        int criticalCount = 0;
        int nonCriticalCount = 0;
        int errorCount = 0;

        for (int i = 0; i < numOfIssues; i++) {
            if (violations.get(i).getCriticality() == Violation.Criticality.CRITICAL) {
                criticalCount++;
            }
            else if (violations.get(i).getCriticality() == Violation.Criticality.NON_CRITICAL) {
                nonCriticalCount++;
            }
            else {
                errorCount++;
            }
        }

        // Error Checking
        if (errorCount != 0 || numOfCritical != criticalCount || numOfNonCritical != nonCriticalCount) {
            Log.e(TAG, "The Number of Critical Issues or Non-Critical Issues does not match to the violations");
            Log.e(TAG, violations.toString());
            this.numOfCritical = criticalCount;
            this.numOfNonCritical = nonCriticalCount;
        }
    }

    // Getters
    public long getInspectionId() {
        return inspectionId;
    }

    public void setInspectionId(long inspectionId) {
        this.inspectionId = inspectionId;
    }

    public long getOwnerRestaurantId() {
        return ownerRestaurantId;
    }

    public void setOwnerRestaurantId(long ownerRestaurantId) {
        this.ownerRestaurantId = ownerRestaurantId;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

//    public int getInspectionYear() {
//        return inspectionYear;
//    }
//    public int getInspectionMonth() {
//        return inspectionMonth;
//    }
//    public int getInspectionDay() {
//        return inspectionDay;
//    }
    public InspectionType getInspectionType() {
        return inspectionType;
    }

    public void setInspectionType(InspectionType inspectionType) {
        this.inspectionType = inspectionType;
    }

    public int getNumOfCritical() {
        return numOfCritical;
    }

    public void setNumOfCritical(int numOfCritical) {
        this.numOfCritical = numOfCritical;
    }

    public int getNumOfNonCritical() {
        return numOfNonCritical;
    }

    public void setNumOfNonCritical(int numOfNonCritical) {
        this.numOfNonCritical = numOfNonCritical;
    }

    public HazardLevel getHazardLevel() {
        return hazardLevel;
    }

    public void setHazardLevel(HazardLevel hazardLevel) {
        this.hazardLevel = hazardLevel;
    }

    public ArrayList<Violation> getViolations() {
        return violations;
    }

    public String getFormattedDate() {
        return formattedDate;
    }

    public void setFormattedDate(String formattedDate) {
        this.formattedDate = formattedDate;
    }

    public Date getInspectionDate(){
        return inspectionDate;
    }

    public void setInspectionDate(Date inspectionDate) {
        this.inspectionDate = inspectionDate;
    }

    //    public String getInspectionDate() {
//        return MONTH[inspectionMonth -1] + " " + inspectionDay + ", " + inspectionYear;
//    }
    public String getInspectionType_S() {
        String message = INSPECTION_TYPE_S[0];
        switch (inspectionType) {
            case ROUTINE:
                message = INSPECTION_TYPE_S[0];
                break;
            case FOLLOW_UP:
                message = INSPECTION_TYPE_S[1];
                break;
        }
        return message;
    }
    public String getHazardLevel_S() {
        String message = HAZARD_LEVEL_S[0];
        switch (hazardLevel) {
            case LOW:
                message = HAZARD_LEVEL_S[0];
                break;
            case MODERATE:
                message = HAZARD_LEVEL_S[1];
                break;
            case HIGH:
                message = HAZARD_LEVEL_S[2];
                break;
        }
        return message;
    }

    @Override
    public String toString() {
        return "Inspection{" +
                "inspectionId=" + inspectionId +
                ", ownerRestaurantId=" + ownerRestaurantId +
                ", trackingNumber='" + trackingNumber + '\'' +
                ", inspectionDate=" + inspectionDate +
                ", formattedDate='" + formattedDate + '\'' +
                ", inspectionType=" + inspectionType +
                ", numOfCritical=" + numOfCritical +
                ", numOfNonCritical=" + numOfNonCritical +
                ", hazardLevel=" + hazardLevel +
                ", violations=" + violations +
                '}';
    }

    // Get all the criticality of all violations
    // Critical for true, otherwise false
    public boolean[] getAllCritical() {
        int numOfIssues = violations.size();
        boolean[] allCritical = new boolean[numOfIssues];
        for (int i = 0; i < numOfIssues; i++) {
            if (violations.get(i).getCriticality() == Violation.Criticality.CRITICAL)
                allCritical[i] = true;
            else
                allCritical[i] = false;
        }
        return allCritical;
    }

    // Get all the nature of all violations
    public int[][] getAllNatures() {
        int numOfIssues = violations.size();
        int[][] allNatures = new int[numOfIssues][5];

        for (int i = 0; i < numOfIssues; i++) {
            for (int j = 0; j < 5; j++) {
                allNatures[i][j] = violations.get(i).getNature()[j];
            }
        }
        return allNatures;
    }

    // Get all the short descriptions
    public String[] getAllShortDescriptions() {
        int numOfIssues = violations.size();
        String[] allShortDescriptions = new String[numOfIssues];
        for (int i = 0; i < numOfIssues; i++) {
            allShortDescriptions[i] = "";
        }

        for (int i = 0; i < numOfIssues; i++) {
            boolean allDoesNotExist = true;
            for (int j = 0; j < 5; j++) {
                if (violations.get(i).getNature()[j] != 0) {
                    allDoesNotExist = false;
                }
            }

            if (allDoesNotExist) {
                allShortDescriptions[i] = "Other Problem";
            }
        }

        for (int i = 0; i < numOfIssues; i++) {
            boolean atLeastOne = false;

            for (int j = 0; j < 5; j++) {
                if (violations.get(i).getNature()[j] != 0) {
                    if (atLeastOne)
                        allShortDescriptions[i] += "/";

                    String temp = Violation.NATURE_KEYWORDS[j];
                    temp = temp.substring(0, 1).toUpperCase() + temp.substring(1);
                    allShortDescriptions[i] += temp;
                    atLeastOne = true;
                }
            }
            allShortDescriptions[i] += " Problem";
        }
        return allShortDescriptions;
    }

    // Get all the long descriptions
    public String[] getAllLongDescriptions() {
        int numOfIssues = violations.size();
        String[] allLongDescriptions = new String[numOfIssues];
        for (int i = 0; i < numOfIssues; i++) {
            allLongDescriptions[i] = violations.get(i).getLongDescription();
        }
        return allLongDescriptions;
    }
}