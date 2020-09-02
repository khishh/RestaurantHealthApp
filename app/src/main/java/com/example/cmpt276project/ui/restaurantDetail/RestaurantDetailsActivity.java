package com.example.cmpt276project.ui.restaurantDetail;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
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
import com.example.cmpt276project.util.SharedPreferencesHelper;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * RestaurantDetailsActivity
 *
 * This activity receives the uuid of the restaurant (Restaurant::restaurantId) a user clicked in the previous activity (RestaurantListActivity)
 * through intent. Using that id, this activity can show the details of that restaurant and show all the inspections of that restaurant.
 *
 * This activity shows the details about the restaurant a user clicked in the RestaurantListActivity and
 * the list of all inspections happened before.
 *
 * A user can click a inspection in a list and it will start the next activity (InspectionDetailsActivity)
 * Users can mark/un-mark a displayed restaurant as their favourite restaurant by clicking on the star icon button.
 *
 * A user can see the location of the displayed restaurant in the Google Map by clicking on its GPS Coordinates.
 * It will take a user back to the MapFragment.
 *
 */
public class RestaurantDetailsActivity extends AppCompatActivity {

    private static final String TAG = RestaurantDetailsActivity.class.getSimpleName();
    public static final String KEY_RESTAURANT_ID = "restaurant_id";

    private ActivityRestaurantDetailsBinding binding;
    private RestaurantDetailsAdapter adapter;
    private RestaurantDetailViewModel viewModel;

    private SharedPreferencesHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_restaurant_details);

        helper = SharedPreferencesHelper.getInstance(getApplicationContext());

        Intent intent = getIntent();
        long restaurantId = intent.getLongExtra(KEY_RESTAURANT_ID, 1);

        viewModel = ViewModelProviders.of(this).get(RestaurantDetailViewModel.class);
        viewModel.loadInspections(restaurantId);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        observeViewModel();

        initializeRecyclerView();

        // mark or un-mark the displayed restaurant
        binding.imageIsFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isFav = binding.getRestaurant().isFav();
                viewModel.updateIsFavouriteInDataBase(restaurantId, isFav);
                setIsFavIcon(!isFav);
            }
        });

        binding.GPSText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "GPS text clicked " + restaurantId);
                helper.saveLastVisitedRestaurant(restaurantId);
                helper.saveRestaurantListActivityState(true);
                finish();
            }
        });
    }


    private void observeViewModel() {

        viewModel.getRestaurant().observe(this, new Observer<Restaurant>() {
            @Override
            public void onChanged(Restaurant restaurant) {
                binding.setRestaurant(restaurant);
                setIsFavIcon(restaurant.isFav());
            }
        });

        viewModel.getInspectionList().observe(this, new Observer<List<Inspection>>() {
            @Override
            public void onChanged(List<Inspection> inspections) {
                setVisibilityOfNoInspectionMsg(inspections.size() == 0);
                adapter.updateInspections(inspections);
            }
        });

    }

    private void setIsFavIcon(boolean isFav){
        if(isFav){
            binding.imageIsFav.setImageResource(R.drawable.ic_baseline_star_24);
        }
        else{
            binding.imageIsFav.setImageResource(R.drawable.ic_baseline_star_border_24);
        }
    }

    private void setVisibilityOfNoInspectionMsg(boolean isEmpty){
        if(isEmpty){
            binding.noInspectionMsg.setVisibility(View.VISIBLE);
        }
        else{
            binding.noInspectionMsg.setVisibility(View.GONE);
        }
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
            SpannableString gps_underlined = new SpannableString(gps);
            gps_underlined.setSpan(new UnderlineSpan(), 0, gps.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            tv.setText(gps_underlined);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        onBackPressed();
        return true;
    }
}