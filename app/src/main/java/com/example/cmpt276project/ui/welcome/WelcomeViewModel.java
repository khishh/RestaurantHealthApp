package com.example.cmpt276project.ui.welcome;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.cmpt276project.R;
import com.example.cmpt276project.model.LoadLocalCSV;
import com.example.cmpt276project.model.Inspection;
import com.example.cmpt276project.model.database.dao.MainDataBase;
import com.example.cmpt276project.model.database.dao.InspectionDao;
import com.example.cmpt276project.model.Restaurant;
import com.example.cmpt276project.model.database.dao.RestaurantDao;
import com.example.cmpt276project.model.database.dao.ViolationDao;
import com.example.cmpt276project.util.SharedPreferencesHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WelcomeViewModel extends AndroidViewModel {

    private final static String TAG = WelcomeViewModel.class.getSimpleName();
    private final static String RESTAURANTS_POST_JSON = "https://data.surrey.ca/api/3/action/package_show?id=restaurants";
    private final static String INSPECTIONS_POST_JSON = "https://data.surrey.ca/api/3/action/package_show?id=fraser-health-restaurant-inspection-reports";

    private final static String RESTAURANT_CSV_REMOTE_PATH = "https://data.surrey.ca/dataset/3c8cb648-0e80-4659-9078-ef4917b90ffb/resource/0e5d04a2-be9b-40fe-8de2-e88362ea916b/download/restaurants.csv";
    private final static String INSPECTION_CSV_REMOTE_PATH = "https://data.surrey.ca/dataset/948e994d-74f5-41a2-b3cb-33fa6a98aa96/resource/30b38b66-649f-4507-a632-d5f6f5fe87f1/download/fraserhealthrestaurantinspectionreports.csv";

    private static final String JSON_GROUP1 = "result";
    private static final String JSON_GROUP2 = "resources";
    private static final String JSON_ITEM1 = "format";
    private static final String JSON_ITEM2 = "last_modified";
    private static final String JSON_ITEM3 = "url";

    private boolean isRestaurantReportUpdated;
    private boolean isInspectionReportUpdated;

    private String newUpdateRecord;
    private Set<String> favouriteRestaurantTrackingNumberSet = new HashSet<>();

    private MutableLiveData<Boolean> isRestaurantUpdateNeeded = new MutableLiveData<>();
    private MutableLiveData<Boolean> isInspectionUpdateNeeded = new MutableLiveData<>();
    private MutableLiveData<Boolean> isDownloadingFromRemote = new MutableLiveData<>();
    private MutableLiveData<Boolean> isSavingDataIntoDataBase = new MutableLiveData<>();
    private MutableLiveData<Boolean> isUpdateCompleted = new MutableLiveData<>();

    private CheckUpdateThread restaurantUpdateCheckThread;
    private CheckUpdateThread inspectionUpdateCheckThread;
    private LoadLocalCSVThread loadLocalCSVThread;
    private LoadRemoteCSVThread loadRemoteCSVThread;
    private ClearDataBaseThread clearDataBaseThread;

    private SharedPreferencesHelper helper = SharedPreferencesHelper.getInstance(getApplication());
    private RestaurantDao restaurantDao = MainDataBase.getInstance(getApplication()).restaurantDao();
    private InspectionDao inspectionDao = MainDataBase.getInstance(getApplication()).inspectionDao();
    private ViolationDao violationDao = MainDataBase.getInstance(getApplication()).violationDao();

    public WelcomeViewModel(@NonNull Application application) {
        super(application);
    }

    public void checkUpdate(){
        isRestaurantReportUpdated = true;
        isInspectionReportUpdated = true;

        restaurantUpdateCheckThread = new CheckUpdateThread(RESTAURANTS_POST_JSON);
        inspectionUpdateCheckThread = new CheckUpdateThread(INSPECTIONS_POST_JSON);

        restaurantUpdateCheckThread.start();
        inspectionUpdateCheckThread.start();

        Log.e(TAG, "newUpdateRecord == " + newUpdateRecord);
    }

    private boolean isReportUpdated(String _url){

        try{
            URL url = new URL(_url);

            HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
            httpURLConnection.setConnectTimeout(5000);
            httpURLConnection.setReadTimeout(2500);
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setDoInput(true);
            httpURLConnection.connect();

            BufferedReader br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            String content_Json = br.readLine();

            JSONObject object = new JSONObject(content_Json).getJSONObject(JSON_GROUP1).getJSONArray(JSON_GROUP2)
                    .getJSONObject(0);

            String lastUpdatedDate = object.getString(JSON_ITEM2);
            String lastModifiedDate;

            if(_url.compareTo(RESTAURANTS_POST_JSON) == 0){
                lastModifiedDate = helper.getLastModifiedDateRestaurant();
            }
            else{
                lastModifiedDate = helper.getLastModifiedDateInspection();
            }

            Log.e(TAG,  lastUpdatedDate + " " + lastModifiedDate);

            if(lastUpdatedDate.compareTo(lastModifiedDate) > 0){

//                // if there is no newUpdateRecord yet
//                if(newUpdateRecord == null){
//                    newUpdateRecord = lastUpdatedDate;
//                }
//                // if both csv files have an update, keep the latest update of either restaurant report or inspection report
//                else{
//                    if(lastUpdatedDate.compareTo(newUpdateRecord) > 0){
//                        newUpdateRecord = lastUpdatedDate;
//                    }
//                }

//                Log.d(TAG, "passed True");
                // JSON says there is an update since the app had loaded data last time
                if(_url.compareTo(RESTAURANTS_POST_JSON) == 0){
                    helper.saveLastModifiedDateRestaurant(lastUpdatedDate);
                    Log.e(TAG, "new LastModifiedDateRestaurant == " + lastUpdatedDate);
                    isRestaurantReportUpdated = true;
                    isRestaurantUpdateNeeded.postValue(true);
                }
                else{
                    helper.saveLastModifiedDateInspection(lastUpdatedDate);
                    Log.e(TAG, "new LastModifiedDateInspection == " + lastUpdatedDate);
                    isInspectionReportUpdated = true;
                    isInspectionUpdateNeeded.postValue(true);
                }
                return true;
            }

        } catch (MalformedURLException e) {
            Log.e(TAG, "URL error : " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "Download failed : " + e.getMessage());
        } catch (JSONException e) {
            Log.e(TAG, "Catch failed : " + e.getMessage());
        }

        if(_url.compareTo(RESTAURANTS_POST_JSON) == 0){
            isRestaurantReportUpdated = false;
            isRestaurantUpdateNeeded.postValue(false);
        }
        else{
            isInspectionReportUpdated = false;
            isInspectionUpdateNeeded.postValue(false);
        }

        if(!isRestaurantReportUpdated && !isInspectionReportUpdated){
            isUpdateCompleted.postValue(true);
        }

        return false;
    }

    public void saveInitialDataSetIntoDataBase(){
        Log.e(TAG, "saving initial data set started");
        LoadLocalCSVThread thread = new LoadLocalCSVThread();
        thread.start();
    }

    public void readNewRestaurantFromRemote(){
        Log.e(TAG, "load remote CSV called");
        if(loadRemoteCSVThread == null) {
            Log.e(TAG, "load remote CSV started");
            loadRemoteCSVThread = new LoadRemoteCSVThread();
            loadRemoteCSVThread.start();
        }
        if(clearDataBaseThread == null){
            Log.e(TAG, "clear local DB started");
            clearDataBaseThread = new ClearDataBaseThread();
            clearDataBaseThread.start();
        }
    }

    public MutableLiveData<Boolean> getIsInspectionUpdateNeeded() {
        return isInspectionUpdateNeeded;
    }

    public MutableLiveData<Boolean> getIsRestaurantUpdateNeeded() {
        return isRestaurantUpdateNeeded;
    }

    public MutableLiveData<Boolean> getIsUpdateCompleted() {
        return isUpdateCompleted;
    }

    public MutableLiveData<Boolean> getIsDownloadingFromRemote() {
        return isDownloadingFromRemote;
    }

    public MutableLiveData<Boolean> getIsSavingDataIntoDataBase() {
        return isSavingDataIntoDataBase;
    }

    private class CheckUpdateThread extends Thread{

        private String json_path;

        public CheckUpdateThread(String path){
            json_path = path;
        }

        @Override
        public void run() {
            isReportUpdated(json_path);
        }
    }

    private List<Restaurant> readLocalCSV(){
        InputStream restaurantSrc = getApplication().getResources().openRawResource(R.raw.restaurants);
        InputStream inspectionSrc = getApplication().getResources().openRawResource(R.raw.inspectionreports);

        LoadLocalCSV dataProcessing = new LoadLocalCSV(restaurantSrc,inspectionSrc);
        List<Restaurant> restaurants = dataProcessing.read();
        return restaurants;
    }

    private void saveDataIntoDataBase(List<Restaurant> restaurants){
        Log.e(TAG, "saveDataIntoDataBase started");

        isSavingDataIntoDataBase.postValue(true);

        List<Long> restaurantIds = restaurantDao.insertRestaurant(restaurants);

        // id version -- works but takes great amount of time
        for(int i = 0; i < restaurants.size(); i++){
            for(Inspection _inspection : restaurants.get(i).getInspection()){
                _inspection.setOwnerRestaurantId(restaurantIds.get(i));
            }

            List<Long> inspectionIds = inspectionDao.insertInspection(restaurants.get(i).getInspection());
            for(int j = 0; j < restaurants.get(i).getInspection().size(); j++){
                Inspection _inspection = restaurants.get(i).getInspection().get(j);
                for(int k = 0; k < _inspection.getViolations().size(); k++){
                    _inspection.getViolations().get(k).setOwnerInspectionId(inspectionIds.get(j));
                }
                // insert violations into database
                violationDao.insertViolation(_inspection.getViolations());
            }
        }

        isSavingDataIntoDataBase.postValue(false);

//        //testing
//        List<Restaurant> restaurantsTest = dao.getAllRestaurants();
//        List<Inspection> inspectionsTest = dao.getAllInspections();
//        Log.e(TAG, "violation Test " + inspectionsTest.get(0).toString() + " id: " + inspectionsTest.get(1).getInspectionId());
//        List<Violation> violationsTest = dao.getViolationsOfOneInspection(inspectionsTest.get(1).getInspectionId());
//
//        for(Restaurant res : restaurantsTest){
//            Log.e(TAG, res.toString());
//        }
//
//        for(Inspection inspectionTest : inspectionsTest) {
//            Log.e(TAG, inspectionTest.toString());
//        }
//
//        Log.e(TAG, "violation: inspectionList size == " + inspectionsTest.size());
//        Log.e(TAG, "violation orig " + restaurants.get(1).getInspection().get(1).toString());
//        Log.e(TAG, "violation result " + violationsTest.size() + " " + violationsTest.toString());
//
//        for(Violation violation : violationsTest){
//            Log.e(TAG, "violation res " + violation.toString());
//        }
    }

    private class LoadLocalCSVThread extends Thread{

        @Override
        public void run() {
            /*
            InputStream restaurantSrc = getApplication().getResources().openRawResource(R.raw.restaurants);
            InputStream inspectionSrc = getApplication().getResources().openRawResource(R.raw.inspectionreports);

            GetData dataProcessing = new GetData(restaurantSrc,inspectionSrc);
            List<Restaurant> initialRestaurants = dataProcessing.read();
             */

            List<Restaurant> restaurants = readLocalCSV();
            saveDataIntoDataBase(restaurants);

            /*
            List<Long> restaurantIds = dao.insertRestaurant(initialRestaurants);

            for(int i = 0; i < initialRestaurants.size(); i++){
                for(Inspection _inspection : initialRestaurants.get(i).getInspection()){
                    _inspection.setOwnerRestaurantId(restaurantIds.get(i));

//                    Log.e(TAG, initialRestaurants.get(i).getTrackingNumber() + " == " + _inspection.getTrackingNumber());
                }

                List<Long> inspectionIds = dao.insertInspection(initialRestaurants.get(i).getInspection());
                for(int j = 0; j < initialRestaurants.get(i).getInspection().size(); j++){
                    Inspection _inspection = initialRestaurants.get(i).getInspection().get(j);
//                    Log.e(TAG, "check _inspection size == " + _inspection.getViolations().size());
                    for(int k = 0; k < _inspection.getViolations().size(); k++){
                        _inspection.getViolations().get(k).setOwnerInspectionId(inspectionIds.get(j));
//                        Log.e(TAG, "Check " + inspectionIds.get(j) + " " + _inspection.getViolations().get(k).getOwnerInspectionId());
                    }

                    // insert violations into database
                    dao.insertViolation(_inspection.getViolations());
                }
            }

            //testing
            List<Restaurant> restaurantsTest = dao.getAllRestaurants();
            List<Inspection> inspectionsTest = dao.getAllInspections();
            Log.e(TAG, "violation Test " + inspectionsTest.get(0).toString() + " id: " + inspectionsTest.get(1).getInspectionId());
            List<Violation> violationsTest = dao.getViolationsOfOneInspection(inspectionsTest.get(1).getInspectionId());

            for(Restaurant res : restaurantsTest){
                Log.e(TAG, res.toString());
            }

            for(Inspection inspectionTest : inspectionsTest) {
                Log.e(TAG, inspectionTest.toString());
            }

            Log.e(TAG, "violation: inspectionList size == " + inspectionsTest.size());

            Log.e(TAG, "violation orig " + initialRestaurants.get(1).getInspection().get(1).toString());

            Log.e(TAG, "violation result " + violationsTest.size() + " " + violationsTest.toString());

            for(Violation violation : violationsTest){
                Log.e(TAG, "violation res " + violation.toString());
            }
             */

            isUpdateCompleted.postValue(true);
        }
    }

    private List<Restaurant> readRemoteCSVs(){

        isDownloadingFromRemote.postValue(true);

        URL url_restaurant = null;
        URL url_inspection = null;
        List<Restaurant> restaurants = new ArrayList<>();

        try{
            url_restaurant = new URL(RESTAURANT_CSV_REMOTE_PATH);
            url_inspection = new URL(INSPECTION_CSV_REMOTE_PATH);
        }catch(MalformedURLException e){
            e.printStackTrace();
        }

        try{
            assert url_restaurant != null;
            assert url_inspection != null;
            URLConnection connection_restaurant = url_restaurant.openConnection();
            URLConnection connection_inspection = url_inspection.openConnection();

            BufferedReader br = new BufferedReader(new InputStreamReader(connection_restaurant.getInputStream()));
            String line = br.readLine();
            while((line = br.readLine()) != null){
                String[] tokens = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
//                System.out.println(Arrays.toString(tokens));
                Restaurant restaurant = new Restaurant();
                restaurant.setTrackingNumber(tokens[0]);
                restaurant.setName(tokens[1].replace("\"", ""));
                restaurant.setAddress(tokens[2]);
//                System.out.println(tokens[5] + " " + tokens[6]);
                restaurant.setLatitude(Double.parseDouble(tokens[5]));
                restaurant.setLongitude(Double.parseDouble(tokens[6]));

                if(favouriteRestaurantTrackingNumberSet.contains(tokens[0])){
                    restaurant.setFav(true);
                }
                else {
                    restaurant.setFav(false);
                }

                restaurants.add(restaurant);
            }
            br.close();

            Map<String, List<Inspection>> inspectionMap = new HashMap<>();
            for(Restaurant _restaurant : restaurants){
                inspectionMap.put(_restaurant.getTrackingNumber(), new ArrayList<>());
            }

            BufferedReader br2 = new BufferedReader(new InputStreamReader(connection_inspection.getInputStream()));

            String line2 = br2.readLine();
            while((line2 = br2.readLine()) != null){

                String[] tokens = line2.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
//                Log.e(TAG, Arrays.toString(tokens));
                if(tokens[0].length() > 0) {
                    Inspection inspection = new Inspection(tokens[0],
                            tokens[1],
                            tokens[2],
                            tokens[3],
                            tokens[4],
                            tokens[6],
                            tokens[5]);
                    String curKey = tokens[0];
                    if (inspectionMap.containsKey(curKey)) {
                        List<Inspection> curInspection = inspectionMap.get(curKey);
                        curInspection.add(inspection);
                        inspectionMap.put(curKey, curInspection);
                    }
                }
            }
            br2.close();

            for(Restaurant restaurant : restaurants){
                String trackingNumber = restaurant.getTrackingNumber();
                if(inspectionMap.containsKey(trackingNumber)){
                    List<Inspection> inspectionList = inspectionMap.get(trackingNumber);
                    restaurant.setInspection(inspectionList);
                    restaurant.computeHazardLevelColor();
                    restaurant.computeLastInspectionDate();
                    restaurant.computeTotalNumIssues();
                }
//                Log.e(TAG, restaurant.toString());
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }

        isDownloadingFromRemote.postValue(false);

        Log.e(TAG, "Fetch finished!!");

        return restaurants;
    }

    private class LoadRemoteCSVThread extends Thread{

        @Override
        public void run() {
            List<Restaurant> restaurants = readRemoteCSVs();
            saveDataIntoDataBase(restaurants);
            isUpdateCompleted.postValue(true);
        }
    }

    private class ClearDataBaseThread extends Thread{

        @Override
        public void run() {
            List<Restaurant> favouriteRestaurantList = restaurantDao.getAllFavRestaurant(true);
            for(Restaurant restaurant : favouriteRestaurantList){
                favouriteRestaurantTrackingNumberSet.add(restaurant.getTrackingNumber());
            }
            restaurantDao.deleteAllRestaurant();
            inspectionDao.deleteAllInspection();
            violationDao.deleteAllViolation();
        }
    }


    @Override
    protected void onCleared() {
        super.onCleared();

        if(restaurantUpdateCheckThread != null){
            restaurantUpdateCheckThread.interrupt();
            restaurantUpdateCheckThread = null;
        }

        if(inspectionUpdateCheckThread != null){
            inspectionUpdateCheckThread.interrupt();
            inspectionUpdateCheckThread = null;
        }

        if(loadLocalCSVThread != null){
            loadLocalCSVThread.interrupt();
            loadLocalCSVThread = null;
        }

        if(loadRemoteCSVThread != null){
            loadRemoteCSVThread.interrupt();
            loadRemoteCSVThread = null;
        }

        if(clearDataBaseThread != null){
            clearDataBaseThread.interrupt();
            clearDataBaseThread = null;
        }
    }
}
