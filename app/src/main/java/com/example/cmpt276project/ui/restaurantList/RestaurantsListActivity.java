package com.example.cmpt276project.ui.restaurantList;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.cmpt276project.R;
import com.example.cmpt276project.ui.restaurantDetail.RestaurantDetailsActivity;
import com.example.cmpt276project.util.SharedPreferencesHelper;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * RestaurantListActivity
 *
 * This activity has two child fragments. Only one of two will be rendered inside FrameLayout in this activity at the time.
 * The map/list icon on the toolbar will control switching fragments to one another.
 *
 * The search icon on the toolbar is collapsed SearchView. Once a user clicks the icon, SearchView and filtering-area would appear.
 * In this filtering area, users can add additional criteria by manipulating UI components,
 *      1. name of the restaurant (query) == the text a user typed inside autocomplete TextView
 *      2. hazard level color (queryColor) == {"All", "Low", "Moderate", "High"}
 *      3. show only users' favourite restaurant (isFav)
 *      4. the lower bound of total number of issues restaurants have (queryMin)
 *      5. the higher bound of total number of issues restaurants have (queryMax)
 *
 * This activity will restart itself after getting location permission granted by a user to redraw Map at the current location of a user.
 *
 * This activity will also start the next activity (RestaurantDetailActivity) as either a user clicks a peg on MapFragment or clicks a item in recyclerview in ListFragment
 *
 * When this activity move onto RestaurantDetailActivity, it passes the uuid of the restaurant a user clicked to RestaurantDetailActivity.
 *
 */

public class RestaurantsListActivity extends AppCompatActivity {

    private static final String TAG = RestaurantsListActivity.class.getSimpleName();
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1337;

    private RestaurantListViewModel viewModel;
    private SharedPreferencesHelper helper;

    /**
     * adapter for autocomplete. receiving the list of restaurant names so that suggestion of
     * restaurant will appear as users type letters in autocomplete
     */
    private ArrayAdapter<String> adapter;

    /**
     * if Map is shown to users, isMapShown is true. Otherwise false
     * Default screen setting is showing map to a user
     */
    private boolean isMapShown = true;

    /**
     * search filter variables
     */
    private String query;
    private String queryColor;
    private boolean queryIsFav;
    private int queryMin;
    private int queryMax;

    /**
     * UI components
     */
    private LinearLayout filterContainer;
    private Spinner colorInput;
    private SwitchMaterial isFavInput;
    private Spinner minInput;
    private Spinner maxInput;
    private Button resetBtn;
    private SearchView searchView;
    SearchView.SearchAutoComplete autoComplete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurants_list);

        helper = SharedPreferencesHelper.getInstance(getApplicationContext());

        // retrieve filter values
        query = helper.getQuery();
        queryColor = helper.getQueryColor();
        queryIsFav = helper.getFav();
        queryMin = helper.getQueryMin();
        queryMax = helper.getQueryMax();
        isMapShown= helper.getRestaurantListActivityState();

        List<String> emptyNameList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, emptyNameList);

        Log.e(TAG, query + "  " + queryColor + "  " + queryIsFav + "  " + queryMin + "  " + queryMax);

        setUpUIComponents();

        viewModel = ViewModelProviders.of(this).get(RestaurantListViewModel.class);
        viewModel.fetchFilteredRestaurant(query, queryColor, queryIsFav, queryMin, queryMax);
        observeViewModel();

        Toolbar toolbar = findViewById(R.id.restaurant_list_toolbar);
        setSupportActionBar(toolbar);

        setUpValuesForSpinners();

        if(isMapShown){
            createRestaurantMapFragment();
        }
        else{
            createRestaurantListFragment();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        isMapShown = helper.getRestaurantListActivityState();

        if(isMapShown){
            createRestaurantMapFragment();
        }
        else{
            createRestaurantListFragment();
        }
    }

    /**
     * Set up values for Spinners' item
     */
    private void setUpValuesForSpinners(){
        // this is a boilerplate code. can improve
        // let ArrayAdapter have the value from 0 to 100 and Min or Max as its initial value
        String[] minRange = new String[102];
        String[] maxRange = new String[102];
        minRange[0] = getString(R.string.search_filter_min);
        maxRange[0] = getString(R.string.search_filter_max);
        for(int i = 0; i < 101; i++){
            minRange[i+1] = String.valueOf(i);
            maxRange[i+1] = String.valueOf(i);
        }

        ArrayAdapter<String> minAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, minRange);
        ArrayAdapter<String> maxAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, maxRange);
        minInput.setAdapter(minAdapter);
        maxInput.setAdapter(maxAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_restaurant_list, menu);
        MenuItem searchMenu = menu.findItem(R.id.menu_search);

        searchMenu.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                Log.d(TAG, "menu expanded");
                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_from_top);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {}

                    @Override
                    public void onAnimationEnd(Animation animation) { filterContainer.setVisibility(View.VISIBLE); }

                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });
                filterContainer.setAnimation(animation);
                animation.start();

                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                Log.d(TAG, "menu collapsed");
                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_to_up);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {}

                    @Override
                    public void onAnimationEnd(Animation animation) { filterContainer.setVisibility(View.GONE);}

                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });
                filterContainer.setAnimation(animation);
                animation.start();
                filterContainer.setVisibility(View.GONE);
                return true;
            }
        });

        searchView = (SearchView) searchMenu.getActionView();
        setUpSearchView();

        return true;
    }

    /**
     * set reference to all UI components
     */
    private void setUpUIComponents(){
        filterContainer = findViewById(R.id.filter_conatiner_parent);
        colorInput = findViewById(R.id.spinner_color);
        isFavInput = findViewById(R.id.switch_isFav);
        minInput = findViewById(R.id.min_range);
        maxInput = findViewById(R.id.max_range);
        resetBtn = findViewById(R.id.btn_reset);

        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.equals(query, "") || !TextUtils.equals(queryColor, "All") || queryIsFav || queryMin != 0 || queryMax != 100) {
                    searchView.clearFocus();
                    helper.saveFilters("", "All", false, 0, 100);
                    Intent intent = new Intent(RestaurantsListActivity.this, RestaurantsListActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    /**
     * Observe any changes to ViewModel's data
     */
    private void observeViewModel(){

        viewModel.getRestaurantNameIdMap().observe(this, new Observer<HashMap<String, Long>>() {
            @Override
            public void onChanged(HashMap<String, Long> stringLongHashMap) {
                Log.e(TAG, "restaurant name is ready");
                adapter.clear();
                List<String> restaurantNameList = viewModel.getAllRestaurantName();
                adapter.addAll(restaurantNameList);
                adapter.notifyDataSetChanged();
            }
        });
    }

    /**
     *  Create RestaurantListFragment and set it to FrameLayout
     *  not added to back stack
     */
    public void createRestaurantListFragment(){
        RestaurantListFragment listFragment = RestaurantListFragment.newInstance();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.restaurant_list_container, listFragment).commit();
    }

    /**
     *  Create RestaurantMapFragment and set it to FrameLayout
     *  not added to back stack
     */
    public void createRestaurantMapFragment(){
        RestaurantMapFragment mapFragment = RestaurantMapFragment.newInstance();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.restaurant_list_container, mapFragment).commit();
    }

    /**
     *  Called from a child fragment. Method to move to RestaurantDetailsFragment.
     */
    public void moveToRestaurantDetailActivity(long restaurantId){
        // save the previous screen state before moving onto the next activity
        helper.saveRestaurantListActivityState(isMapShown);

        Intent intent = new Intent(RestaurantsListActivity.this, RestaurantDetailsActivity.class);
        // putExtra will come here include Long id
        intent.putExtra(RestaurantDetailsActivity.KEY_RESTAURANT_ID, restaurantId);
        startActivity(intent);
    }

    /**
     * Search filter variables inside SharedPreferences so that the new RestaurantListActivity will be
     * able to only fetch restaurant meet all filtering requirements.
     */
    private void saveFiltersIntoPreference(String query){
        String color = colorInput.getSelectedItem().toString();
        boolean isFav = isFavInput.isChecked();
        String minStr = (String) minInput.getSelectedItem();
        String maxStr = (String) maxInput.getSelectedItem();

        if(TextUtils.equals(minStr, getResources().getString(R.string.search_filter_min))){
            queryMin = 0;
        }
        else{
            queryMin = Integer.parseInt(minStr);
        }

        if(TextUtils.equals(maxStr, getResources().getString(R.string.search_filter_max))){
            queryMax = 100;
        }
        else{
            queryMax = Integer.parseInt(maxStr);
        }

        helper.saveFilters(query, color, isFav, queryMin, queryMax);
    }


    @SuppressLint("RestrictedApi")
    private void setUpSearchView(){
        searchView.setSubmitButtonEnabled(true);

        SearchManager searchManager = (SearchManager)getSystemService(Context.SEARCH_SERVICE);
        assert searchManager != null;
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            // when a user clicks a suggestion in a list, get the uuid of that restaurant (long)
            // and start RestaurantDetailActivity with the id.
            @Override
            public boolean onSuggestionClick(int position) {
                String selectedItem = (String) adapter.getItem(position);
                long selectedItemId = viewModel.getRestaurantIdByName(selectedItem);
                moveToRestaurantDetailActivity(selectedItemId);
                return true;
            }
        });

        autoComplete = searchView.findViewById(R.id.search_src_text);
        autoComplete.setHint(R.string.search_filter_restaurant_name);
        autoComplete.setHintTextColor(getResources().getColor(R.color.white_bg));
        if(query.length() > 0){
            autoComplete.setText(query);
        }

        autoComplete.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                Log.e(TAG, "user clicked keyboard enter");
                String query = v.getText().toString();
                saveFiltersIntoPreference(query);
                helper.saveRestaurantListActivityState(isMapShown);
                return false;
            }
        });

        autoComplete.setAdapter(adapter);
        autoComplete.setThreshold(1);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "onQueryTextSubmit " + query);
                FrameLayout container = findViewById(R.id.restaurant_list_container);
                container.setVisibility(View.GONE);
                helper.saveRestaurantListActivityState(isMapShown);
                saveFiltersIntoPreference(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG, "onQueryTextChange " + newText);
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){

            case R.id.menu_switch:
                Log.d(TAG, "switch menu clicked");
                if(!isMapShown){
                    createRestaurantMapFragment();
                    item.setIcon(R.drawable.ic_baseline_format_list_bulleted_24);
                    isMapShown = true;
                    setTitle(R.string.restaurant_list_activity_title_map);
                }
                else{
                    createRestaurantListFragment();
                    item.setIcon(R.drawable.ic_baseline_map_24);
                    isMapShown = false;
                    setTitle(R.string.restaurant_list_activity_title_list);
                }
                break;

        }
        return true;
    }

    public void requestLocationPermission(){
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        Log.d(TAG, "onRequestPermissionsResult called");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "onRequestPermissionsResult: Granted");

                    // redraw map in order to show the current location of a user at the center
                    finishAffinity();
                    Intent intent = new Intent(RestaurantsListActivity.this, RestaurantsListActivity.class);
                    startActivity(intent);

                } else {
                    Log.d(TAG, "onRequestPermissionsResult: Rejected");
                    Toast.makeText(getApplicationContext(), R.string.restaurant_list_activity_permission_denied, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

}