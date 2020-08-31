/*
    This is InspectionDetailsActivity class implementation.
    Last Modified Date: 2020/07/08

    This class is an Activity which display the details of the inspection.
    This class includes a RecycleView which requires to use InspectionAdapter Class.

    This class uses 9 images from the online
    Citation:
    Image: led_green, Website: https://www.iconexperience.com/v_collection/icons/?icon=led_green
    Image: led_yellow, Website: https://www.iconexperience.com/v_collection/icons/?icon=led_yellow
    Image: led_red, Website: https://www.iconexperience.com/v_collection/icons/?icon=led_red
    Image: hazard_level, Website: https://en.wikipedia.org/wiki/Hazard_symbol#/media/File:DIN_4844-2_Warnung_vor_einer_Gefahrenstelle_D-W000.svg
    Image: food_violation, Website: https://www.pngegg.com/en/png-zenqs
    Image: utensil_violation, Website: https://www.pngegg.com/en/png-zdoua
    Image: equipment_violation, Website: https://www.pngegg.com/en/png-zlpju
    Image: pest_violation, Website: https://www.pngegg.com/en/png-znbhf/download
    Image: employee_violation, Website: https://www.pngegg.com/en/png-dvzjj/download

    // This function only uses for testing
    testing_initialization();
 */

// Package
package com.example.cmpt276project.ui.inspectionDetail;

// Import
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.BindingAdapter;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cmpt276project.R;
import com.example.cmpt276project.databinding.ActivityInspectionDetailsBinding;
import com.example.cmpt276project.model.Inspection;
import com.example.cmpt276project.model.RestaurantManager;
import com.example.cmpt276project.model.Violation;

import java.util.ArrayList;
import java.util.List;

// InspectionDetailsActivity Class
public class InspectionDetailsActivity extends AppCompatActivity {

    private static final String TAG = InspectionDetailsActivity.class.getSimpleName();
    public static final String KEY_INSPECTION_ID = "inspection_id";

    private long inspectionId;

    private ActivityInspectionDetailsBinding binding;
    private InspectionViewModel viewModel;
    InspectionAdapter inspectionAdapter;

    int images[] = {R.drawable.equipment_violation, R.drawable.utensil_violation,
            R.drawable.food_violation, R.drawable.pest_violation, R.drawable.employee_violation,
            R.drawable.hazard_level};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_inspection_details);

        Intent intent = getIntent();
        inspectionId = intent.getLongExtra(KEY_INSPECTION_ID, 1);

        viewModel = ViewModelProviders.of(this).get(InspectionViewModel.class);
        viewModel.loadViolations(inspectionId);
        observeViewModel();

        //Actionbar Button
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        updateRecycleView();
    }

    private void observeViewModel() {

        viewModel.getInspection().observe(this, new Observer<Inspection>() {
            @Override
            public void onChanged(Inspection inspection) {
                binding.setInspection(inspection);
            }
        });

        viewModel.getViolationList().observe(this, new Observer<List<Violation>>() {
            @Override
            public void onChanged(List<Violation> violations) {
                setVisibilityOfNoViolationMsg(violations.size() == 0);
                inspectionAdapter.updateViolations(violations);
            }
        });
    }

    private void setVisibilityOfNoViolationMsg(boolean isEmpty){
        if(isEmpty){
            binding.noViolationMsg.setVisibility(View.VISIBLE);
        }
        else{
            binding.noViolationMsg.setVisibility(View.GONE);
        }
    }

    // Set the RecyclerView
    private void updateRecycleView() {
        RecyclerView recyclerView = binding.inspectionRecycleView;

        // Set the adapter to the RecyclerView
        inspectionAdapter = new InspectionAdapter(new ArrayList<>(), images);
        recyclerView.setAdapter(inspectionAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @BindingAdapter("android:textColor")
    public static void setHazardTextColor(TextView tv, Inspection.HazardLevel hazardLevel){
        if(hazardLevel == null){
            return;
        }
        int colorId;
        switch(hazardLevel) {
            case LOW:
                colorId = tv.getContext().getColor(R.color.Inspection_color_green);
                break;
            case MODERATE:
                colorId = tv.getContext().getColor(R.color.Inspection_color_yellow);
                break;
            case HIGH:
                colorId = tv.getContext().getColor(R.color.Inspection_color_red);
                break;
            default:
                colorId = 0;
        }

        tv.setTextColor(colorId);
    }

    @BindingAdapter("android:srcColor")
    public static void setHazardColorIcon(ImageView imageView, Inspection.HazardLevel hazardLevel){
        if(hazardLevel == null){
            return;
        }
        int iconId;
        switch(hazardLevel) {
            case LOW:
                iconId = R.drawable.led_green;
                break;
            case MODERATE:
                iconId = R.drawable.led_yellow;
                break;
            case HIGH:
                iconId = R.drawable.led_red;
                break;
            default:
                iconId = 0;
        }
        imageView.setImageResource(iconId);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        onBackPressed();
        return true;
    }
}