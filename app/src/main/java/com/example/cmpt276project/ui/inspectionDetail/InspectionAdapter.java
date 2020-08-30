/*
    This is InspectionAdapter class implementation.
    Last Modified Date: 2020/07/08

    This class requires to input 6 parameters.
    1. context: Context
    2. allCritical: boolean[][]
    3. allNatures: int[][]
    4. allShortDescription String[]
    5. allLongDescription String[]
    6. images: int[]

    This class uses for to be a RecyclerView Adapter to display detail of each violation
    in the inspection.
 */

// Package
package com.example.cmpt276project.ui.inspectionDetail;

// Import
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cmpt276project.R;
import com.example.cmpt276project.databinding.ListItemViolationBinding;
import com.example.cmpt276project.model.Violation;

import java.util.List;

// InspectionAdapter Class
public class InspectionAdapter extends RecyclerView.Adapter<InspectionAdapter.InspectionViewHolder> {

//    private boolean[] allCritical;
////    private int[][] allNatures;
////    private String[] allShortDescriptions;
////    private String[] allLongDescriptions;

    private List<Violation> violationList;
    private int[] images;

    private ListItemViolationBinding binding;

    public InspectionAdapter(List<Violation> violations, int[] images){
        this.violationList = violations;
        this.images = images;
    }

    public void updateViolations(List<Violation> violations){
        violationList.clear();
        violationList.addAll(violations);
        notifyDataSetChanged();
    }

    // Link to violation_row.xml document
    @NonNull
    @Override
    public InspectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        binding = DataBindingUtil.inflate(inflater, R.layout.list_item_violation, parent, false);
        return new InspectionViewHolder(binding);
    }

    // Set functions to each row
    @Override
    public void onBindViewHolder(@NonNull final InspectionViewHolder holder, int position) {

        Violation violation = violationList.get(position);

        binding.setViolation(violation);
        // Set the number of violations TextView
        String positionMsg = (position + 1) + ":";
        holder.binding.numViolationsTextView.setText(positionMsg);

        // Set the critical TextView and ImageView
        if (violation.getCriticality() == Violation.Criticality.CRITICAL) {
            holder.binding.criticalTextView.setText(R.string.InspectionApter_critical);
            holder.binding.criticalTextView.setTextColor(Color.parseColor("#FF0000"));
            holder.binding.isCriticalImageView.setImageResource(images[5]);
        }
        else {
            holder.binding.criticalTextView.setText(R.string.InspectionApter_not_critical);
            holder.binding.isCriticalImageView.setImageResource(0);
        }

        // Set the Nature ImageView
        ImageView[] imageViews = {holder.binding.equipmentImage, holder.binding.utensilImage,
                holder.binding.foodImage, holder.binding.pestImage, holder.binding.employeeImage};

        for (int i = 0; i < 5; i++) {
            if (violation.getNature()[i] != 0) {
                imageViews[i].setImageResource(images[i]);
            }
            else {
                imageViews[i].setImageResource(0);
            }
        }

        // Set the Short Description TextView
        holder.binding.problemList.setText(violation.getDescription());

        // Set each row of layout to make Toast
        holder.binding.violationConstraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(holder.binding.violationConstraintLayout.getContext(), violation.getLongDescription(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Set the number of rows
    @Override
    public int getItemCount() {
        return violationList.size();
    }

    // Set all the TextViews, ImageViews, and ConstrainLayout
    public class InspectionViewHolder extends RecyclerView.ViewHolder {

        ListItemViolationBinding binding;

        public InspectionViewHolder(@NonNull ListItemViolationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
