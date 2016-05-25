package edu.cs65.caregiver.caregiver.controllers;

import android.content.Context;

import edu.cs65.caregiver.caregiver.model.CareGiver;

/**
 * Created by don on 5/23/16.
 */
public class DataController {

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
}
