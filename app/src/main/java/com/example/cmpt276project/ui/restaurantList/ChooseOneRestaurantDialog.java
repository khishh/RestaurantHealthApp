package com.example.cmpt276project.ui.restaurantList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.cmpt276project.R;

import java.util.ArrayList;
import java.util.List;

public class ChooseOneRestaurantDialog extends AppCompatDialogFragment {

    List<ClusterItems> list;

    public ChooseOneRestaurantDialog(List<ClusterItems> items){
        list = items;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        List<ClusterItems> list = new ArrayList<>(clickedCluster.getItems());
        List<String> strList = new ArrayList<>();
        for (ClusterItems item : list){
            String hazardLevel = item.getmHazardLevel();
            if(hazardLevel == null){
                hazardLevel = "No Inspections Available Yet";
            }
            strList.add(item.getTitle() + " (" + hazardLevel + ")");
        }
        String[] items = strList.toArray(new String[0]);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.choose_dialog_selection_message)
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((RestaurantsListActivity)getActivity()).moveToRestaurantDetailActivity(list.get(which).getId());
                    }
                });
        return builder.create();
    }
}