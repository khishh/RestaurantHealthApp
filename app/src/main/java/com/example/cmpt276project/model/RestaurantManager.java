package com.example.cmpt276project.model;

import java.util.ArrayList;
import java.util.List;

//Desc: This is an ArrayList of restaurants that is used to store the data of the restaurants
public class RestaurantManager {

    private static RestaurantManager instance;

    private List<Restaurant> restaurants = new ArrayList<>();

    private RestaurantManager(){}

    public static RestaurantManager getInstance(){

        if(instance == null){
            instance = new RestaurantManager();
        }
        return instance;
    }

    public void setRestaurants(List<Restaurant> restaurants) {
        this.restaurants = restaurants;
    }

    public List<Restaurant> getRestaurants() {
        return restaurants;
    }

    public void add(Restaurant restaurant){
        restaurants.add(restaurant);
    }

    public Restaurant getRestaurant(int index){
        return restaurants.get(index);
    }

    public List<Inspection> getInspectionsAtIndex(int index){
        return restaurants.get(index).getInspection();
    }
}
