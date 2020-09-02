package com.example.cmpt276project.ui.welcome;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.cmpt276project.R;
import com.example.cmpt276project.ui.restaurantList.RestaurantsListActivity;
import com.example.cmpt276project.util.SharedPreferencesHelper;

/**
 *
 * WelcomeActivity
 *
 * Case1 : If the app launches for the first time
 *  -> 1. read the two local csv files stored in res/raw with LoadLocalCSV class
 *  -> 2. Store loaded data into database
 *  -> 3. move onto the next activity (RestaurantListActivity)
 *
 * Case2 : If the app had launched at least one time
 * Check if 20 hours has passed since the last launched time?
 *   -> 20 hours passed: ask users if they like to check if there is any new data available on remote CSVs?
 *      ->No: no need to check for an update, move onto the next activity
 *      ->Yes: check last modified date of each CSV file on remote
 *          -> if new update found
 *              -> 1. read new data from remote CSVs
 *              -> 2. save new data into database
 *              -> 3. after 2, move onto the next activity
 *          -> no update found
 *              -> move onto the next activity
 *   -> Not 20 hrs passes: move onto the next activity
 *
 *   This activity will only control UI, and all other data related works (reading, fetching, and saving) are done in WelcomeViewModel
 *
 */

public class WelcomeActivity extends AppCompatActivity
    implements UpdateDialog.UpdateDialogListener {

    private final static String TAG = WelcomeActivity.class.getSimpleName();

    private WelcomeViewModel viewModel;
    private SharedPreferencesHelper helper;

    private DownloadDialog downloadDialog;
    private SaveDBDialog saveDBDialog;

    // 20 hours
    private final static long refreshTime = 20 * 60 * 60 * 1000L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        viewModel = ViewModelProviders.of(this).get(WelcomeViewModel.class);
        helper = SharedPreferencesHelper.getInstance(this);

        // reset the filtering values used in RestaurantsListActivity
        resetFiltersAndState();
        observeViewModel();

        // get the last updated time for this app. 0 means this is the first launch.
        long prevTime = helper.getLastLaunchTIme();

        // first launch time, so need to save content in raw file into db
        if(prevTime == 0){
            Log.e(TAG, "prevTime == 0");
            helper.saveUpdateTime(1L);
            // call viewModel to load the initial data set from res/raw csv files
            viewModel.saveInitialDataSetIntoDataBase();
        }
        else if(is20hoursPassed()){
            // ask users if they like to check any update on csv reports on remote
            long currentMilliTime = System.currentTimeMillis();
            helper.saveUpdateTime(currentMilliTime);
            showUpdateDialog();
        }
        else{
            // if within 20 hours, no need to check updates. proceed to the next activity
            moveToRestaurantListActivity();
        }
    }

    private void resetFiltersAndState() {
        helper.saveFilters("", "All", false, 0, 100);
        helper.saveRestaurantListActivityState(true);
        helper.saveLastVisitedRestaurant(0);
    }

    private void observeViewModel() {

        // observe if Restaurant CSV has updated since the last updated time
        viewModel.getIsRestaurantUpdateNeeded().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean) {
                    // need to update restaurant
                    Log.e(TAG, "restaurant update need!");
                    showDownloadDialog();
                    viewModel.readNewRestaurantFromRemote();
                }
                else {
                    Log.e(TAG, " restaurant update no need");
                }
            }
        });

        // observe if Inspection CSV has updated since the last updated time
        viewModel.getIsInspectionUpdateNeeded().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean) {
                    // need to update inspection
                    Log.e(TAG, "inspection update need!");
                    viewModel.readNewRestaurantFromRemote();
                }
                else {
                    Log.e(TAG, "inspection update no need");
                }
            }
        });

        // observe when loading remote CSV files starts and ends
        viewModel.getIsDownloadingFromRemote().observe(this, aBoolean -> {
            if(aBoolean){
                Log.e(TAG, "remote download started");
                showDownloadDialog();
            }
            else{
                Log.e(TAG, "remote download finished");
                downloadDialog.dismiss();
            }
        });

        // observe when saving new data into DB starts and ends
        viewModel.getIsSavingDataIntoDataBase().observe(this, aBoolean -> {
            if(aBoolean){
                Log.e(TAG, "data saving into DB started");
                showSavingDialog();
            }
            else{
                Log.e(TAG, "data saving into DB finished");
                saveDBDialog.dismiss();
            }
        });

        // observe if this activity is ready to move onto the next activity (== data is ready and saved in DB!)
        viewModel.getIsUpdateCompleted().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    Log.e(TAG, "update completed!");
                    moveToRestaurantListActivity();
                }
            }
        });

    }

    /**
     * check if 20 hours has passed since the app launched for the last time
     */
    private boolean is20hoursPassed(){
        long prevTime = helper.getLastLaunchTIme();
        long currentTime = System.currentTimeMillis();

        if((currentTime - prevTime) >= refreshTime ){
            return true;
        }
        else{
            return false;
        }
    }

    private void showUpdateDialog() {
        UpdateDialog dialog = new UpdateDialog();
        dialog.setCancelable(false);
        dialog.show(getSupportFragmentManager(), "UpdateDialog");
    }

    private void showDownloadDialog(){
        if(downloadDialog == null){
            downloadDialog = new DownloadDialog();
            downloadDialog.setCancelable(false);
            downloadDialog.show(getSupportFragmentManager(), "download_dialog");
        }
    }

    private void showSavingDialog(){
        saveDBDialog = new SaveDBDialog();
        saveDBDialog.setCancelable(false);
        saveDBDialog.show(getSupportFragmentManager(), "saving_dialog");
    }

    public void moveToRestaurantListActivity(){
        Intent intent = new Intent(WelcomeActivity.this, RestaurantsListActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onUpdateDialogPositiveClick() {
        viewModel.checkUpdate();
        Toast.makeText(this, "Checking update.." , Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpdateDialogNegativeClick() {
        moveToRestaurantListActivity();
    }
}