package com.example.cmpt276project.ui.restaurantDetail;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cmpt276project.R;
import com.example.cmpt276project.databinding.ListItemInspectionsBinding;
import com.example.cmpt276project.databinding.ListItemViolationBinding;
import com.example.cmpt276project.model.Inspection;

import java.util.List;

//This adapter handles the setup of the RestaurantDetailsActivity recyclerView and handles formating for the date in the recyclerView
public class RestaurantDetailsAdapter extends RecyclerView.Adapter<RestaurantDetailsAdapter.ViewHolder> {

    private static final String TAG = "RestaurantDetailsAdapter";

    private List<Inspection> inspectionList;
    private InspectionAdapterItemClickListener inspectionAdapterItemClickListener;

//    private ListItemInspectionsBinding binding;

    public RestaurantDetailsAdapter(List<Inspection> inspectionList, InspectionAdapterItemClickListener inspectionAdapterItemClickListener) {
        this.inspectionList = inspectionList;
        this.inspectionAdapterItemClickListener = inspectionAdapterItemClickListener;
    }

    public void updateInspections(List<Inspection> inspections){
        inspectionList.clear();
        inspectionList.addAll(inspections);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RestaurantDetailsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_inspections, parent, false);
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ListItemInspectionsBinding binding = DataBindingUtil.inflate(inflater, R.layout.list_item_inspections, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantDetailsAdapter.ViewHolder holder, final int position) {

        Inspection inspection = inspectionList.get(position);
        holder.binding.setInspection(inspection);

        switch (inspection.getHazardLevel()) {
            case LOW:
                holder.binding.parentLayout.setBackgroundResource(R.drawable.list_bg_green);
                holder.binding.hazardImageView.setImageResource(R.drawable.led_green);
                break;

            case MODERATE:
                holder.binding.parentLayout.setBackgroundResource(R.drawable.list_bg_yellow);
                holder.binding.hazardImageView.setImageResource(R.drawable.led_yellow);
                break;

            case HIGH:
                holder.binding.parentLayout.setBackgroundResource(R.drawable.list_bg_red);
                holder.binding.hazardImageView.setImageResource(R.drawable.led_red);
                break;
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long inspectionId = inspectionList.get(position).getInspectionId();
                inspectionAdapterItemClickListener.onItemClicked(inspectionId);
            }
        });

    }

    @Override
    public int getItemCount() {
        return inspectionList.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder {

        ListItemInspectionsBinding binding;

        public ViewHolder(@NonNull ListItemInspectionsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}