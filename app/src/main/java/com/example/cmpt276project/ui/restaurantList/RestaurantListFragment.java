package com.example.cmpt276project.ui.restaurantList;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.cmpt276project.databinding.FragmentRestaurantListBinding;
import com.example.cmpt276project.model.Restaurant;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * RestaurantListFragment
 *
 * Fragment containing RecyclerView where all restaurants meet user's criteria will be displayed.
 * Users can click a row of RecyclerView and it will proceed to the next activity (RestaurantDetailActivity)
 *
 * User can click on the button with a heart icon to mark a restaurant as the favourite.
 *
 */
public class RestaurantListFragment extends Fragment {

    private static final String TAG = RestaurantListFragment.class.getSimpleName();

    private FragmentRestaurantListBinding binding;
    private RestaurantListViewModel viewModel;
    private RestaurantListAdapter adapter;

    public RestaurantListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment RestaurantListFragment.
     */
    public static RestaurantListFragment newInstance() {
        RestaurantListFragment fragment = new RestaurantListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentRestaurantListBinding.inflate(inflater, container, false);
        viewModel = ViewModelProviders.of(requireActivity()).get(RestaurantListViewModel.class);
        observeViewModel();
        return binding.getRoot();    
    }

    private void observeViewModel() {
        viewModel.getRestaurantList().observe(getViewLifecycleOwner(), new Observer<List<Restaurant>>() {
            @Override
            public void onChanged(List<Restaurant> restaurantList) {
                Log.e(TAG, "restaurant updated!!");
                setVisibilityOfNoRestaurantMsg(restaurantList.size() == 0);
                adapter.updateRestaurant(restaurantList);
            }
        });
    }

    private void setVisibilityOfNoRestaurantMsg(boolean isEmpty){
        if (isEmpty) {
            binding.noRestaurantMsg.setVisibility(View.VISIBLE);
        } else {
            binding.noRestaurantMsg.setVisibility(View.GONE);
        }
    }

    /**
     *  Set up RecyclerView
     */
    private void setUpRecyclerView(){

        adapter = new RestaurantListAdapter(new ArrayList<>());
        adapter.setListener(listener);

        /*
        SnappingLinearLayoutManager linearLayoutManager = new SnappingLinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.recycleview_divier));

        binding.listRecyclerView.addItemDecoration(dividerItemDecoration);
         */

        binding.listRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.listRecyclerView.setAdapter(adapter);

        if(adapter.getItemCount() == 0){
            binding.noRestaurantMsg.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        setUpRecyclerView();
    }

    /**
     *  Handle user click events on each item inside RecyclerView
     *  onClick -> when a whole area of item was clicked
     */
    private RestaurantListAdapter.Listener listener = new RestaurantListAdapter.Listener() {

        @Override
        //start RestaurantDetailsActivity based on the position of item clicked
        public void onClick(long restaurantId) {
            ((RestaurantsListActivity) getActivity()).moveToRestaurantDetailActivity(restaurantId);
        }
    };

}