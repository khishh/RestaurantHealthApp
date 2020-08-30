package com.example.cmpt276project.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Date;
import java.util.List;

//Desc: This class is used to store the information of every single restaurant including the trackingNumber, name, address, etc. and a list of Inspection which are inspected from that restaurant
@Entity
public class Restaurant {

    @NonNull
    @PrimaryKey(autoGenerate = true)
    private long restaurantId;

    private String trackingNumber;
    private String name;
    private String address;
    private double latitude;
    private double longitude;
    private int iconRestaurant;

    private int totalNumIssues = 0;
    private String hazardLevelColor;
    private String lastInspectionDateFormatted;

    @Ignore
    private List<Inspection> inspection;

    public Restaurant(){
    }

    // ignore this constructor for room
    @Ignore
    public Restaurant(String trackingNumber, String name, String address, double latitude, double longitude){
        this.trackingNumber = trackingNumber;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Accessor


    public long getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(long restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getIconRestaurant() {
        return iconRestaurant;
    }

    public void setIconRestaurant(int iconRestaurant) {
        this.iconRestaurant = iconRestaurant;
    }

    public int getTotalNumIssues() {
//        if(totalNumIssues == 0){
//            computeTotalNumIssues();
//        }
        return totalNumIssues;
    }

    public void computeTotalNumIssues() {
        for (int i=0; i<inspection.size();i++){
            totalNumIssues+=inspection.get(i).getNumOfCritical()
                    + inspection.get(i).getNumOfNonCritical();
        }
    }

    public void setTotalNumIssues(int totalNumIssues) {
        this.totalNumIssues = totalNumIssues;
    }

    public String getHazardLevelColor() {
        return hazardLevelColor;
    }

    public void setHazardLevelColor(String hazardLevelColor) {
        this.hazardLevelColor = hazardLevelColor;
    }

    public void computeHazardLevelColor(){
        if(inspection.size() > 0){
            hazardLevelColor = inspection.get(0).getHazardLevel_S();
        }
    }

    public void computeLastInspectionDate() {
        if(inspection.size() > 0) {
            this.lastInspectionDateFormatted = inspection.get(0).getFormattedDate();
        }
    }

    public String getLastInspectionDateFormatted() {
        return lastInspectionDateFormatted;
    }

    public void setLastInspectionDateFormatted(String lastInspectionDateFormatted) {
        this.lastInspectionDateFormatted = lastInspectionDateFormatted;
    }

    public List<Inspection> getInspection() {
        return inspection;
    }

    public void setInspection(List<Inspection> inspection) {
//        for(int n=inspection.size()-1; n>=0; n--)
//            for(int i=0; i<n; i++)
//                if(inspection.get(i).getInspectionDate().before(inspection.get(i + 1).getInspectionDate())) {
//                    Inspection temp = inspection.get(i);
//                    inspection.set(i, inspection.get(i+1));
//                    inspection.set(i+1, temp);
//                }
        this.inspection = inspection;
    }

    // Get the latest inspection{
    public Inspection getTheMostRecentInspection(){
        if(inspection.size() > 0){
            return inspection.get(0);
        }
        return null;
    }

    @Override
    public String toString() {
        return "Restaurant{" +
                "restaurantId=" + restaurantId +
                ", trackingNumber='" + trackingNumber + '\'' +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", iconRestaurant=" + iconRestaurant +
                ", totalNumIssues=" + totalNumIssues +
                ", hazardLevelColor='" + hazardLevelColor + '\'' +
                ", lastInspectionDate=" + lastInspectionDateFormatted +
                ", inspection=" + inspection +
                '}';
    }
}
