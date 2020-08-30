package com.example.cmpt276project.ui.restaurantDetail;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.BindingAdapter;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cmpt276project.R;
import com.example.cmpt276project.databinding.ActivityRestaurantDetailsBinding;
import com.example.cmpt276project.model.Inspection;
import com.example.cmpt276project.model.Restaurant;
import com.example.cmpt276project.ui.inspectionDetail.InspectionDetailsActivity;

import java.util.ArrayList;
import java.util.List;

//The Activity for the second screen, handles setting up the screen and recyclerView, passes list index position to third activity
public class RestaurantDetailsActivity extends AppCompatActivity {

    private static final String TAG = RestaurantDetailsActivity.class.getSimpleName();
    public static final String KEY_RESTAURANT_ID = "restaurant_id";

    private ActivityRestaurantDetailsBinding binding;
    private RestaurantDetailsAdapter adapter;
    private RestaurantDetailViewModel viewModel;

    private long restaurantId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_restaurant_details);

        Intent intent = getIntent();
        restaurantId = intent.getLongExtra(KEY_RESTAURANT_ID, 1);

        viewModel = ViewModelProviders.of(this).get(RestaurantDetailViewModel.class);
        viewModel.loadInspections(restaurantId);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        observeViewModel();

        initializeRecyclerView();
    }

    private void observeViewModel() {

        viewModel.getRestaurant().observe(this, new Observer<Restaurant>() {
            @Override
            public void onChanged(Restaurant restaurant) {
                binding.setRestaurant(restaurant);
            }
        });

        viewModel.getInspectionList().observe(this, new Observer<List<Inspection>>() {
            @Override
            public void onChanged(List<Inspection> inspections) {
                adapter.updateInspections(inspections);
            }
        });

    }

    private void initializeRecyclerView() {
        RecyclerView recyclerView = binding.InspectionView;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RestaurantDetailsAdapter(new ArrayList<>(), new InspectionAdapterItemClickListener() {
            @Override
            public void onItemClicked(long inspectionId) {
                Intent intent = new Intent(RestaurantDetailsActivity.this, InspectionDetailsActivity.class);
                intent.putExtra(InspectionDetailsActivity.KEY_INSPECTION_ID, inspectionId);
                Log.e(TAG, "inspectionId == " + inspectionId);
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    @BindingAdapter("android:setGPS")
    public static void setGPS(TextView tv, Restaurant restaurant){
        if(restaurant != null) {
            String gps = restaurant.getLatitude() + "  " + restaurant.getLongitude();
            tv.setText(gps);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        onBackPressed();
        return true;
    }
}