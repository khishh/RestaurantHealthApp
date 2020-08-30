package com.example.cmpt276project.ui.restaurantList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.cmpt276project.model.Restaurant;
import com.example.cmpt276project.util.SharedPreferencesHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.example.cmpt276project.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class RestaurantMapFragment extends Fragment {

    private static final String TAG = RestaurantMapFragment.class.getSimpleName();
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1337;
    private static final int MINIMUM_CLUSTER_SIZE = 2;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final float GLOBAL_ZOOM = 15f;
    private static final LatLng defaultLocation = new LatLng(49.14, -122.87);

    private RestaurantListViewModel viewModel;
    private SharedPreferencesHelper helper;

    private GoogleMap myMap;
    private SupportMapFragment mapFragment;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location lastKnownLocation;
    private Location mLocation;
    private LocationRequest mLocationRequest;
    private Marker mCurrLocationMarker;

    private LocationManager mLocationManager;
    private ClusterManager<ClusterItems> mClusterManager;

    private Boolean locationPermissionGranted = false;
    private long lastVisitedRestaurantId = -1;

    private Cluster<ClusterItems> clickedCluster;

    private static final String KEY_SEARCH = "search";
    private static final String KEY_COLOR = "color";
    private static final String KEY_IS_FAV = "isFav";

    private String userSearchInput;
    private String userColorInput;
    private boolean userIsFavInput;

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            mLocation = new Location(location);
            myMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(mLocation.getLatitude(), mLocation.getLongitude())));
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onProviderDisabled(String provider) {}
    };

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                //The last location in the list is the newest
                Location location = locationList.get(locationList.size() - 1);
                Log.i("MapsActivity", "Location: " + location.getLatitude() + " " + location.getLongitude());
                lastKnownLocation = location;
                if (mCurrLocationMarker != null) {
                    mCurrLocationMarker.remove();
                }

                //Place current location marker
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title(getString(R.string.restaurant_map_fragment_current_position));
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                mCurrLocationMarker = myMap.addMarker(markerOptions);

                //move map camera
                myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 11));
            }
        }
    };

    public static RestaurantMapFragment newInstance() {
        RestaurantMapFragment fragment = new RestaurantMapFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private OnMapReadyCallback mapReadyCallback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {

            myMap = googleMap;
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(1000);
            mLocationRequest.setFastestInterval(1000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

            if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if (ContextCompat.checkSelfPermission(getContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED){
                    Log.d(TAG, "onMapReady: Already granted in recent SDK");
                    locationPermissionGranted = true;
                    myMap.setMyLocationEnabled(true);
                    myMap.getUiSettings().setMyLocationButtonEnabled(true);
                }
                else{
                    getLocationPermission();
                }
            }
            else {
                Log.d(TAG, "onMapReady: Already granted in old SDK");
                locationPermissionGranted = true;
                myMap.setMyLocationEnabled(true);
                myMap.getUiSettings().setMyLocationButtonEnabled(true);
            }

            updateLocationUI();
            setUpCluster();
            getDeviceLocation();

            mLocationManager = (LocationManager)getContext().getSystemService(Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, locationListener);
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(Objects.requireNonNull(getContext()));
        helper = SharedPreferencesHelper.getInstance(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        viewModel = ViewModelProviders.of(requireActivity()).get(RestaurantListViewModel.class);
        observeViewModel();
        return inflater.inflate(R.layout.fragment_restaurant_map, container, false);
    }

    private void observeViewModel() {
        viewModel.getRestaurantList().observe(getViewLifecycleOwner(), new Observer<List<Restaurant>>() {
            @Override
            public void onChanged(List<Restaurant> restaurantList) {
                Log.e(TAG, "restaurant updated!!");
                setUpMap();
            }
        });

        viewModel.getRestaurantLastVisited().observe(this, new Observer<Restaurant>() {
            @Override
            public void onChanged(Restaurant restaurant) {
                moveCamera(new LatLng(restaurant.getLatitude(),
                                restaurant.getLongitude()),
                        GLOBAL_ZOOM);
            }
        });
    }

    private void setUpMap(){
        GoogleMapOptions options = new GoogleMapOptions();
        options.compassEnabled(true)
                .zoomControlsEnabled(true)
                .scrollGesturesEnabled(true)
                .zoomGesturesEnabled(true);
        mapFragment = SupportMapFragment.newInstance(options);
        getChildFragmentManager().beginTransaction().replace(R.id.map, mapFragment).commit();

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                Log.d(TAG, "NEW SDK : permission already granted");
                fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                mapFragment.getMapAsync(mapReadyCallback);
            } else {
                //Request Location Permission
                Log.d(TAG, "NEW SDK : permission was not granted");
                getLocationPermission();
            }
        }
        else {
            Log.d(TAG, "OLD SDK : permission already granted");
            fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            mapFragment.getMapAsync(mapReadyCallback);
        }
    }

    private void setUpCluster() {

        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        mClusterManager = new ClusterManager<ClusterItems>(getContext(), myMap);

        ClusterItemsMarkerRenderer renderer = new ClusterItemsMarkerRenderer(getContext(), myMap, mClusterManager);
        renderer.setMinClusterSize(MINIMUM_CLUSTER_SIZE);
        mClusterManager.setRenderer(renderer);

        myMap.setInfoWindowAdapter(mClusterManager.getMarkerManager());

        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<ClusterItems>() {
            @Override
            public boolean onClusterItemClick(ClusterItems item) {
                Log.d(TAG, "clusterItem clicked");
                return false;
            }
        });

        mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<ClusterItems>() {
            @Override
            public boolean onClusterClick(Cluster<ClusterItems> cluster) {
                if(myMap.getMaxZoomLevel()-4 <= myMap.getCameraPosition().zoom)
                    clickedCluster = cluster;
                else {
                    clickedCluster = null;
                    // increment zoom level
                    float nextZoomLevel = myMap.getCameraPosition().zoom + 1;
                    CameraUpdate zoomedLocation = CameraUpdateFactory.newLatLngZoom(cluster.getPosition(), nextZoomLevel);
                    myMap.animateCamera(zoomedLocation);
                }
                return false;
            }
        });

        mClusterManager.setOnClusterItemInfoWindowClickListener(new ClusterManager.OnClusterItemInfoWindowClickListener<ClusterItems>() {
            @Override
            public void onClusterItemInfoWindowClick(ClusterItems item) {
                Log.d(TAG, "clusterItem's window clicked");
                ((RestaurantsListActivity) getActivity()).moveToRestaurantDetailActivity(item.getId());
            }
        });

        mClusterManager.setOnClusterInfoWindowClickListener(new ClusterManager.OnClusterInfoWindowClickListener<ClusterItems>() {
            @Override
            public void onClusterInfoWindowClick(Cluster<ClusterItems> cluster) {
                List<ClusterItems> clusterItems = new ArrayList<>(cluster.getItems());
                ChooseOneRestaurantDialog dialog = new ChooseOneRestaurantDialog(clusterItems);
                dialog.show(getChildFragmentManager(), null);
            }
        });

        mClusterManager.getClusterMarkerCollection().setInfoWindowAdapter(new CustomItemAdapterForCluster());

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        myMap.setOnCameraIdleListener(mClusterManager);
        myMap.setOnMarkerClickListener(mClusterManager);
        mClusterManager.setAnimation(false);

        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<ClusterItems>() {
            @Override
            public boolean onClusterItemClick(ClusterItems item) {
                Log.d(TAG, "clusterItem clicked");
                return false;
            }
        });

        mClusterManager.setOnClusterItemInfoWindowClickListener(new ClusterManager.OnClusterItemInfoWindowClickListener<ClusterItems>() {
            @Override
            public void onClusterItemInfoWindowClick(ClusterItems item) {
                Log.d(TAG, "clusterItem's window clicked  " + item.getId());
                ((RestaurantsListActivity)getActivity()).moveToRestaurantDetailActivity(item.getId());
            }
        });

        mClusterManager.setOnClusterInfoWindowClickListener(new ClusterManager.OnClusterInfoWindowClickListener<ClusterItems>() {
            @Override
            public void onClusterInfoWindowClick(Cluster<ClusterItems> cluster) {
                List<ClusterItems> clusterItems = new ArrayList<>(cluster.getItems());
                ChooseOneRestaurantDialog dialog = new ChooseOneRestaurantDialog(clusterItems);
                dialog.show(getChildFragmentManager(), null);
            }
        });

        // Add cluster items (markers) to the cluster manager.
        addItems();

        if(lastVisitedRestaurantId != -1){
            viewModel.fetchOneRestaurantById(lastVisitedRestaurantId);
        }
    }

    private void addItems() {

        // Set some lat/lng coordinates to start with.
        List<Restaurant> restaurantList = viewModel.getRestaurantList().getValue();

        // Add ten cluster items in close proximity, for purposes of this example.
        for (int i = 0; i < restaurantList.size(); i++) {

            Restaurant restaurant = restaurantList.get(i);
            final double lat = restaurant.getLatitude();
            final double lon = restaurant.getLongitude();
            String title = restaurant.getName();
            String hazardLevelString = restaurant.getHazardLevelColor();
            long id = restaurant.getRestaurantId();

            String snippet = (getString(R.string.restaurant_map_fragment_address) + ": " + restaurant.getAddress() + ",    " + getString(R.string.restaurant_map_fragment_hazard_level) + ": " + hazardLevelString);
            ClusterItems offsetItem = new ClusterItems(lat, lon, title, hazardLevelString, snippet, id);

            mClusterManager.addItem(offsetItem);
        }
    }

    private void updateLocationUI() {
        if (myMap == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                myMap.setMyLocationEnabled(true);
                myMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                myMap.setMyLocationEnabled(false);
                myMap.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        Log.d(TAG, "getLocationPermission called");
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.restaurant_map_fragment_location_permission_needed)
                        .setMessage(R.string.restaurant_map_fragment_location_permission_message)
                        .setPositiveButton(R.string.restaurant_map_fragment_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ((RestaurantsListActivity)getActivity()).requestLocationPermission();
                            }
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed, we can request the permission.
                ((RestaurantsListActivity)getActivity()).requestLocationPermission();
            }
        }
    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Current location is successful.");
                            // Set the map's camera position to the current location of the device.
                            lastKnownLocation = task.getResult();
                            if (lastKnownLocation != null) {
                                myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(lastKnownLocation.getLatitude(),
                                                lastKnownLocation.getLongitude()), GLOBAL_ZOOM));
                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            myMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(defaultLocation, GLOBAL_ZOOM));
                            myMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    private void moveCamera(LatLng latLng, float zoom){
        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }


    /**
     * Inner classes
     */

    private class CustomItemAdapterForCluster implements GoogleMap.InfoWindowAdapter {

        private final View contentView;

        CustomItemAdapterForCluster(){
            contentView = getLayoutInflater().inflate(R.layout.list_marker_map, null);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {

            if(clickedCluster != null) {
                RecyclerView recyclerView = contentView.findViewById(R.id.cluster_list);
                TextView textView = contentView.findViewById(R.id.cluster_number);
                String numText = getString(R.string.restaurant_map_fragment_multi_restaurants_message) + ": " + clickedCluster.getSize();
                textView.setText(numText);
                List<ClusterItems> list = new ArrayList<>(clickedCluster.getItems());
                recyclerView.setAdapter(new ClusterItemAdapter(list));
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            }
            else
                return null;
            return contentView;
        }
    }

    class ClusterItemsMarkerRenderer extends DefaultClusterRenderer<ClusterItems> {

        private final IconGenerator mIconGenerator = new IconGenerator(getContext());
        private final IconGenerator mClusterIconGenerator = new IconGenerator(getContext());
        private ImageView mImageView;
        private ImageView mClusterImageView;
        private int mDimension;

        public ClusterItemsMarkerRenderer(Context context, GoogleMap map, ClusterManager<ClusterItems> clusterManager) {
            super(context, map, clusterManager);

            View restaurantPreview = getLayoutInflater().inflate(R.layout.hazard_marker, null);
            mClusterIconGenerator.setContentView(restaurantPreview);
            mClusterImageView = restaurantPreview.findViewById(R.id.hazardMarkerView);

            mImageView = new ImageView(getContext());
            mDimension = (int) getResources().getDimension(R.dimen.restaurant_hazard_level);
            mImageView.setLayoutParams(new ViewGroup.LayoutParams(mDimension, mDimension));
            int padding = (int) getResources().getDimension(R.dimen.restaurant_hazard_padding);
            mImageView.setPadding(padding, padding, padding, padding);
            mIconGenerator.setContentView(mImageView);
        }

        protected void onBeforeClusterItemRendered(ClusterItems item, MarkerOptions markerOptions) {
            markerOptions.icon(getRestaurantIcon(item)).title(item.getTitle());
        }

        private BitmapDescriptor getRestaurantIcon(ClusterItems clusterItems) {
            if(clusterItems.getmHazardLevel() != null) {
                switch (clusterItems.getmHazardLevel()) {
                    case "Low":
                        mImageView.setImageResource(R.drawable.led_green);
                        mImageView.setBackgroundResource(R.drawable.list_bg_green);
                        break;
                    case "Moderate":
                        mImageView.setImageResource(R.drawable.led_yellow);
                        mImageView.setBackgroundResource(R.drawable.list_bg_yellow);
                        break;
                    case "High":
                        mImageView.setImageResource(R.drawable.led_red);
                        mImageView.setBackgroundResource(R.drawable.list_bg_red);
                        break;
                }
            }
            else{
                mImageView.setImageResource(R.drawable.led_white);
                mImageView.setBackgroundResource(0);
            }
            Bitmap icon = mIconGenerator.makeIcon();
            return BitmapDescriptorFactory.fromBitmap(icon);
        }
    }

}