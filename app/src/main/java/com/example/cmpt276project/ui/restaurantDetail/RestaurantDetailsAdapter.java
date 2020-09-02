package com.example.cmpt276project.ui.restaurantDetail;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cmpt276project.R;
import com.example.cmpt276project.databinding.ListItemInspectionsBinding;
import com.example.cmpt276project.model.Inspection;

import java.util.List;

/**
 * RecyclerView adapter for clusters in RestaurantDetailsActivity
 */

public class RestaurantDetailsAdapter extends RecyclerView.Adapter<RestaurantDetailsAdapter.ViewHolder> {

    private static final String TAG = "RestaurantDetailsAdapter";

    private List<Inspection> inspectionList;
    private InspectionAdapterItemClickListener inspectionAdapterItemClickListener;

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