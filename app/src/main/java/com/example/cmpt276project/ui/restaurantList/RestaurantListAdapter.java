package com.example.cmpt276project.ui.restaurantList;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cmpt276project.R;
import com.example.cmpt276project.databinding.ListItemRestaurantBinding;
import com.example.cmpt276project.model.Inspection;
import com.example.cmpt276project.model.Restaurant;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// The custom adapter class for RestaurantListActivity's RecyclerView.

public class RestaurantListAdapter extends RecyclerView.Adapter<RestaurantListAdapter.ViewHolder> {

    private static final String TAG = "RestaurantListAdapter";

    private List<Restaurant> listRestaurant;

    private Listener listener;

    interface Listener{
        void onClick(long position);
    }

    public void setListener(Listener listener){
        this.listener = listener;
    }

    public RestaurantListAdapter(List<Restaurant> restaurantList){
        this.listRestaurant = restaurantList;
    }

    public void updateRestaurant(List<Restaurant> restaurants){
        listRestaurant.clear();
        listRestaurant.addAll(restaurants);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ListItemRestaurantBinding binding = DataBindingUtil.inflate(inflater, R.layout.list_item_restaurant, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

        LinearLayout linearLayout = holder.binding.itemLinearLayout;

        Restaurant restaurant = listRestaurant.get(position);
        holder.binding.setRestaurant(restaurant);

        if(restaurant.getHazardLevelColor()!= null){
            switch (restaurant.getHazardLevelColor()){

                case "Low":
                    linearLayout.setBackgroundResource(R.drawable.list_bg_green);
                    break;

                case "Moderate":
                    linearLayout.setBackgroundResource(R.drawable.list_bg_yellow);
                    break;

                case "High":
                    linearLayout.setBackgroundResource(R.drawable.list_bg_red);

            }
        }

        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null){
                    listener.onClick(restaurant.getRestaurantId());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return listRestaurant.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ListItemRestaurantBinding binding;

        public ViewHolder(@NonNull ListItemRestaurantBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
