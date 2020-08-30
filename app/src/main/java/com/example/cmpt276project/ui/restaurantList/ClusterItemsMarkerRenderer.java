package com.example.cmpt276project.ui.restaurantList;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.cmpt276project.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

public class ClusterItemsMarkerRenderer extends DefaultClusterRenderer<ClusterItems> {

    private final IconGenerator mIconGenerator;
    private final IconGenerator mClusterIconGenerator;
    private ImageView mImageView;
    private ImageView mClusterImageView;
    private int mDimension;
    private Context context;

    public ClusterItemsMarkerRenderer(LayoutInflater inflater, GoogleMap map, ClusterManager<ClusterItems> clusterManager) {
        super(inflater.getContext(), map, clusterManager);

        context = inflater.getContext();

        mIconGenerator = new IconGenerator(context);
        mClusterIconGenerator = new IconGenerator(context);

        View restaurantPreview = inflater.inflate(R.layout.hazard_marker, null);
        mClusterIconGenerator.setContentView(restaurantPreview);
        mClusterImageView = restaurantPreview.findViewById(R.id.hazardMarkerView);

        mImageView = new ImageView(context);
        mDimension = (int) context.getResources().getDimension(R.dimen.restaurant_hazard_level);
        mImageView.setLayoutParams(new ViewGroup.LayoutParams(mDimension, mDimension));
        int padding = (int) context.getResources().getDimension(R.dimen.restaurant_hazard_padding);
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