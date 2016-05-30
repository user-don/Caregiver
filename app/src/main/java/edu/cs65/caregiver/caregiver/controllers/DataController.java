package edu.cs65.caregiver.caregiver.controllers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;

import edu.cs65.caregiver.backend.messaging.model.CaregiverEndpointsObject;
import edu.cs65.caregiver.caregiver.Globals;
import edu.cs65.caregiver.caregiver.R;
import edu.cs65.caregiver.caregiver.model.CareGiver;
import edu.cs65.caregiver.caregiver.model.Recipient;

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
    }

    public void setData(CareGiver newCareGiver) {
        careGiver = newCareGiver;
    }

    public Recipient setRecipientData(Recipient recipient) {
        for (int i = 0; i < careGiver.mRecipients.size(); i++) {
            if (careGiver.mRecipients.get(i).mName.equals(recipient.mName)) {
                careGiver.mRecipients.set(i, recipient);
                return careGiver.mRecipients.get(i);
            }
        }
        return null;
    }

    public void saveData() {
        Gson gson = new Gson();

        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = preferences.edit();

        String saved_data = gson.toJson(careGiver);
        prefsEditor.putString(SAVED_DATA_KEY, saved_data);
        prefsEditor.commit();
    }

    public void saveRegistrationId(String regId) {
        SharedPreferences preferences = context.getSharedPreferences(Globals.REG_PREF_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = preferences.edit();
        prefsEditor.putString("regId", regId);
        prefsEditor.commit();
    }

    public String getRegistrationId() {
        SharedPreferences preferences = context.getSharedPreferences(Globals.REG_PREF_FILE, Context.MODE_PRIVATE);
        return preferences.getString("regId", "");
    }

    public void saveToPreferences(String prefsFileName, String key, String value) {
        SharedPreferences preferences = context.getSharedPreferences(
                prefsFileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = preferences.edit();
        prefsEditor.putString(key, value);
        prefsEditor.apply();
    }

    public String getStringFromPreferences(String prefsFileName, String key) {
        SharedPreferences preferences = context.getSharedPreferences(
                prefsFileName, Context.MODE_PRIVATE);
        return preferences.getString(key, "");
    }

    public void loadData() {
        Gson gson = new Gson();

        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        String data = preferences.getString(SAVED_DATA_KEY, "");
        if (!data.equals("")) {
            careGiver = gson.fromJson(data, CareGiver.class);
            Log.d(TAG, "loaded data: " + data);
        } else {
            Log.d(TAG, "Error loading data occurred");
            careGiver = new CareGiver("NA");
        }

    }
}
