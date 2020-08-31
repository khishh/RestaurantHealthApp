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
 * This activity will:
 *
 *  1.
 *
 */

public class WelcomeActivity extends AppCompatActivity
    implements UpdateDialog.UpdateDialogListener {

    private final static String TAG = WelcomeActivity.class.getSimpleName();

    private WelcomeViewModel viewModel;
    private SharedPreferencesHelper helper;

    private DownloadDialog downloadDialog;
    private SaveDBDialog saveDBDialog;

    // 20 hours 20 * 60 * 60 * 1000L
    private final static long refreshTime = 5 * 60 * 1000L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        viewModel = ViewModelProviders.of(this).get(WelcomeViewModel.class);
        helper = SharedPreferencesHelper.getInstance(this);

        resetFiltersAndState();
        observeViewModel();

        long prevTime = helper.getUpdateTime();

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
    }

    private void showUpdateDialog() {
        UpdateDialog dialog = new UpdateDialog();
        dialog.setCancelable(false);
        dialog.show(getSupportFragmentManager(), "UpdateDialog");
    }

    private void observeViewModel() {

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

    private boolean is20hoursPassed(){
        long prevTime = helper.getUpdateTime();
        long currentTime = System.currentTimeMillis();

        if((currentTime - prevTime) >= refreshTime ){
            return true;
        }
        else{
            return false;
        }
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