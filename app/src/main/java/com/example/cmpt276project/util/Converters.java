package com.example.cmpt276project.util;

import androidx.room.TypeConverter;

import com.example.cmpt276project.model.Inspection;
import com.example.cmpt276project.model.Violation;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Date;

public class Converters {
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public static Inspection.InspectionType stringToInspectionType(String inspectionType_str){
        return inspectionType_str.equals(Inspection.INSPECTION_TYPE_S[0]) ? Inspection.InspectionType.ROUTINE : Inspection.InspectionType.FOLLOW_UP;
    }

    @TypeConverter
    public static String inspectionTypeTpString(Inspection.InspectionType inspectionType){
        return inspectionType == Inspection.InspectionType.ROUTINE ? "Routine" : "Follow-Up";
    }

    @TypeConverter
    public static Inspection.HazardLevel stringToHazardLevel(String hazardLevel_str){
        if(hazardLevel_str.equals(Inspection.HAZARD_LEVEL_S[0])){
            return Inspection.HazardLevel.LOW;
        }
        else if(hazardLevel_str.equals(Inspection.HAZARD_LEVEL_S[1])){
            return Inspection.HazardLevel.MODERATE;
        }
        else{
            return Inspection.HazardLevel.HIGH;
        }
    }

    @TypeConverter
    public static String hazardLevelToString(Inspection.HazardLevel hazardLevel){
        if(hazardLevel == Inspection.HazardLevel.LOW){
            return "Low";
        }
        else if(hazardLevel == Inspection.HazardLevel.MODERATE){
            return "Moderate";
        }
        else{
            return "High";
        }
    }

    @TypeConverter
    public static Violation.Criticality stringToCriticality(String criticality_str){
        if(criticality_str.equals(Violation.CRITICALITY_KEYWORDS[0])){
            return Violation.Criticality.CRITICAL;
        }
        else if(criticality_str.equals(Violation.CRITICALITY_KEYWORDS[1])){
            return Violation.Criticality.NON_CRITICAL;
        }
        else{
            return Violation.Criticality.ERROR;
        }
    }

    @TypeConverter
    public static String criticalityToString(Violation.Criticality criticality){
        if(criticality == Violation.Criticality.CRITICAL){
            return "critical";
        }
        else if(criticality == Violation.Criticality.NON_CRITICAL){
            return "not critical";
        }
        else{
            return "error";
        }
    }

    @TypeConverter
    public static int[] stringToIntArr(String intArr_str){
        try{
            JSONArray jsonArray = new JSONArray(intArr_str);
            int[] intArr = new int[jsonArray.length()];
            for(int i = 0; i < jsonArray.length(); i++){
                intArr[i] = Integer.parseInt(jsonArray.getString(i));
            }
            return intArr;
        }catch(JSONException e){
            e.printStackTrace();
        }
        return null;
    }

    @TypeConverter
    public static String intArrToString(int[] intArr){
        JSONArray jsonArray = new JSONArray();
        for(int value : intArr){
            jsonArray.put(value);
        }
        return jsonArray.toString();
    }
}