package edu.cs65.caregiver.caregiver;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;

import android.os.Handler;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.cs65.caregiver.backend.registration.Registration;
import edu.cs65.caregiver.caregiver.controllers.DataController;

public class MainActivity extends Activity {

    private final int SPLASH_DISPLAY_LENGTH = 3250;

    public static String mRegistrationID;

    private static DataController mDc;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDc = DataController.getInstance(getApplicationContext());
        mDc.initializeData(getApplicationContext());

        //new GcmRegistrationAsyncTask(this).execute();

//        String regId = mDc.getRegistrationId();
//        if (!regId.equals("")) {
//            new GcmUnRegistrationAsyncTask(this, regId).execute();
//            new GcmRegistrationAsyncTask(this).execute();
//        } else {
//            new GcmRegistrationAsyncTask(this).execute();
//        }


        String versionCode = "";
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionCode = String.valueOf(packageInfo.versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String savedVersion = mDc.getStringFromPreferences(Globals.APP_VERSION_PREFS_FILE,
                Globals.PREFS_VERSION_KEY);
        if (savedVersion.equals("")) {
            // First time loading application. Get a new registration ID
            // Deregister if previous registration ID available, and register.
            new GcmRegistrationAsyncTask(this).execute();
            mDc.saveToPreferences(Globals.APP_VERSION_PREFS_FILE, Globals.PREFS_VERSION_KEY, versionCode);
        } else if (!versionCode.equals(savedVersion) || mDc.getRegistrationId().equals("")) {
            // Updated version. Deregister previous registration ID and re-register.
            new GcmUnRegistrationAsyncTask(this, savedVersion).execute();
            new GcmRegistrationAsyncTask(this).execute();
            mDc.saveToPreferences(Globals.APP_VERSION_PREFS_FILE, Globals.PREFS_VERSION_KEY, versionCode);
        }

        // TODO: REMOVE FOR PRODUCTION
        SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.profile_preference), MODE_PRIVATE).edit();
        editor.clear();
        editor.apply();

        SharedPreferences preferences = getSharedPreferences(getString(R.string.profile_preference), 0);

        if (!preferences.getString(Globals.EMAIL_KEY,"").equals("")){
            if (preferences.getString(Globals.ACCNT_KEY,"").equals("care recipient")){
                Intent newMedication = new Intent(getApplicationContext(), CareRecipientActivity.class);
                startActivity(newMedication);
                finish();
            } else if (preferences.getString(Globals.ACCNT_KEY,"").equals("caregiver")){
                Intent newMedication = new Intent(getApplicationContext(), CareGiverActivity.class);
                startActivity(newMedication);
                finish();
            }
        } else {
            setContentView(R.layout.main_landing_page);

            ImageView myImageView= (ImageView)findViewById(R.id.landing_image);
            Animation myFadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fadein);
            myImageView.startAnimation(myFadeInAnimation);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent mainIntent = new Intent(getApplicationContext(), AccountSignOnActivity.class);
                    startActivity(mainIntent);
                    finish();
                }
            }, SPLASH_DISPLAY_LENGTH);
        }
    }

    class GcmRegistrationAsyncTask extends AsyncTask<Void, Void, String> {
        private Registration regService = null;
        private GoogleCloudMessaging gcm;
        private Context context;

        public GcmRegistrationAsyncTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(Void... params) {
            if (regService == null) {
                Registration.Builder builder = new Registration.Builder(AndroidHttp.newCompatibleTransport(),
                        new AndroidJsonFactory(), null)
                        .setRootUrl(Globals.SERVER_ADDR + "/_ah/api/");

                regService = builder.build();
            }

            String msg = "";
            try {
                if (gcm == null) {
                    gcm = GoogleCloudMessaging.getInstance(context);
                }
                // check if registration ID already instantiated

                mRegistrationID = gcm.register(Globals.SENDER_ID);
                mDc.saveRegistrationId(mRegistrationID);
                msg = "Device registered, registration ID = " + mRegistrationID;

                // Send registration ID to server over HTTP so it can use GCM/HTTP
                // to send messages to the app.
                regService.register(mRegistrationID).execute();

            } catch (IOException ex) {
                ex.printStackTrace();
                Log.d(Globals.TAG, "Error: " + ex.getMessage());
                msg = null;
            }
            return msg;
        }

        @Override
        protected void onPostExecute(String msg) {

            //Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
            if (msg != null) {
                Logger.getLogger("REGISTRATION").log(Level.INFO, msg);
                Toast.makeText(context, "Connected to Cloud!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Failed to Connect to Cloud", Toast.LENGTH_SHORT).show();
            }
        }
    }

    class GcmUnRegistrationAsyncTask extends AsyncTask<Void, Void, Void> {
        private Registration regService = null;
        private GoogleCloudMessaging gcm;
        private Context context;
        private String regId;

        public GcmUnRegistrationAsyncTask(Context context) {
            this.context = context;
        }

        public GcmUnRegistrationAsyncTask(Context context, String regId) {
            this.context = context;
            this.regId = regId;

        }

        @Override
        protected Void doInBackground(Void... params) {
            if (regService == null) {
                Registration.Builder builder = new Registration.Builder(AndroidHttp.newCompatibleTransport(),
                        new AndroidJsonFactory(), null)
                        .setRootUrl(Globals.SERVER_ADDR + "/_ah/api/");

                regService = builder.build();
            }

            String msg = "";
            try {
                if (gcm == null) {
                    gcm = GoogleCloudMessaging.getInstance(context);
                }

                if (regId != null) {
                    regService.unregister(regId).execute();
                }
                gcm.unregister();
            } catch (IOException ex) {
                ex.printStackTrace();
                Log.d(Globals.TAG, "Error: " + ex.getMessage());
                msg = null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void msg) {
        }
    }

}
