package edu.cs65.caregiver.caregiver;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

/**
 * Created by McFarland on 5/30/16.
 */
public class AlarmReceiver extends WakefulBroadcastReceiver {
    private static final String TAG = "alarm receiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG,"Calling data reset");

        SharedPreferences mPrefs = context.getSharedPreferences(context.getString(R.string.profile_preference), 0);
        mPrefs.edit().putBoolean("reset", true).commit();

        Intent i = new Intent("edu.cs65.caregiver.caregiver.CareGiverActivity");
        //i.setFlags()
    }
}