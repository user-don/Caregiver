package edu.cs65.caregiver.caregiver.controllers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;

import edu.cs65.caregiver.backend.messaging.model.CaregiverEndpointsObject;
import edu.cs65.caregiver.backend.messaging.model.CaregiverObject;
import edu.cs65.caregiver.caregiver.model.CareGiver;

/**
 * Created by don on 5/23/16.
 */
public class DataController {
    private static final String TAG = "DataController";

    /*
       We can use the singleton design pattern to make the data in our models accessible
       anywhere in the app where application context is available. Matt and I used this
       for our project and it allows us to remove the data model from any given activity.

       Using the DataController
         * Declare as `private static DataController mDataController;`
         * Instantiation:
         `mDataController = DataController.getInstance(getApplicationContext());`
     */

    // Reference this careGiver object whenever you need to access the data model.
    public CareGiver careGiver;

    private Context context;

    private static final String PREFERENCES_FILE = "caregiver preferences";
    private static final String SAVED_DATA_KEY = "saved data key";

    // For instantiating as a singleton
    private static DataController sDataController;
    public static DataController getInstance(@SuppressWarnings("UnusedParameters") Context c) {
        if(sDataController == null) {
            sDataController = new DataController();
        }
        return sDataController;
    }

    public void initializeData(Context context) {
        // call this with application context so that the DataController can access resources etc.
        this.context = context.getApplicationContext();

//        if (careGiver == null) {
//            careGiver = new CareGiver(user);
//            saveData();
//        }
    }

    public void setData(CareGiver newCareGiver) {
        careGiver = newCareGiver;
    }

    public void saveData() {
        Gson gson = new Gson();

        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = preferences.edit();

        String saved_data = gson.toJson(careGiver);
        prefsEditor.putString(SAVED_DATA_KEY, saved_data);
        prefsEditor.commit();
    }

    public void loadData() {
        Gson gson = new Gson();

        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        String data = preferences.getString(SAVED_DATA_KEY, "");
        if (data.equals("")) {
            careGiver = gson.fromJson(data, CareGiver.class);
        } else {
            Log.d(TAG, "Error loading data occurred");
            careGiver = new CareGiver("NA");
        }

    }
}
