package com.example.cmpt276project.ui.restaurantList;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cmpt276project.R;


import java.util.ArrayList;
import java.util.List;

public class ClusterItemAdapter extends RecyclerView.Adapter<ClusterItemAdapter.ViewHolder> {

    private List<ClusterItems> clusterItems = new ArrayList<>();

    public ClusterItemAdapter(List<ClusterItems> cluster){
        clusterItems = cluster;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_marker_map, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ClusterItems item = clusterItems.get(position);

        if(item.getmHazardLevel() != null) {
            switch (item.getmHazardLevel()) {
                case "Low":
                    holder.background.setBackgroundResource(R.drawable.list_bg_green);
                    break;
                case "Moderate":
                    holder.background.setBackgroundResource(R.drawable.list_bg_yellow);
                    break;
                case "High":
                    holder.background.setBackgroundResource(R.drawable.list_bg_red);
                    break;
            }
        }
        else{
            holder.background.setBackgroundResource(0);
        }

        holder.title.setText(item.getTitle());
    }

    @Override
    public int getItemCount() {
        return clusterItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public LinearLayout background;
        public TextView title;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            background = itemView.findViewById(R.id.cluster_item_background);
            title = itemView.findViewById(R.id.cluster_item_name);
        }
    }
}