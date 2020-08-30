package com.example.cmpt276project.ui.inspectionDetail;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.cmpt276project.model.Inspection;
import com.example.cmpt276project.model.RestaurantDao;
import com.example.cmpt276project.model.RestaurantDataBase;
import com.example.cmpt276project.model.Violation;

import java.util.List;

public class InspectionViewModel extends AndroidViewModel {

    private static final String TAG = InspectionViewModel.class.getSimpleName();

    private MutableLiveData<Inspection> inspection = new MutableLiveData<>();
    private MutableLiveData<List<Violation>> violationList = new MutableLiveData<>();

    private FetchViolationsFromDataBase fetchViolationsFromDataBase;

    private RestaurantDao dao = RestaurantDataBase.getInstance(getApplication()).restaurantDao();

    public InspectionViewModel(@NonNull Application application) {
        super(application);
    }

    public void loadViolations(long inspectionId){
        fetchViolationsFromDataBase = new FetchViolationsFromDataBase(inspectionId);
        fetchViolationsFromDataBase.start();
    }

    private void fetchViolation(long inspectionId){

        Log.e(TAG, "inspectionId == " + inspectionId);
        Inspection _inspection = dao.getAInspection(inspectionId);
        List<Violation> _violations = dao.getViolationsOfOneInspection(inspectionId);
        inspection.postValue(_inspection);
        violationList.postValue(_violations);
        Log.e(TAG, _inspection.toString());
        Log.e(TAG, "violations size == " + _violations.size());
        Log.e(TAG, _violations.toString());
    }

    private class FetchViolationsFromDataBase extends Thread{

        private long inspectionId;

        public FetchViolationsFromDataBase(long inspectionId){
            this.inspectionId = inspectionId;
        }

        @Override
        public void run() {
            fetchViolation(inspectionId);
        }
    }

    public MutableLiveData<Inspection> getInspection() {
        return inspection;
    }

    public MutableLiveData<List<Violation>> getViolationList() {
        return violationList;
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        if(fetchViolationsFromDataBase != null){
            fetchViolationsFromDataBase.interrupt();
            fetchViolationsFromDataBase = null;
        }
    }
}
