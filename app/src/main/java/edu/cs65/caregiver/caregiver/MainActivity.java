package edu.cs65.caregiver.caregiver;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.gson.Gson;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.cs65.caregiver.backend.registration.Registration;
import edu.cs65.caregiver.caregiver.model.CareGiver;

public class MainActivity extends Activity {

    private final int SPLASH_DISPLAY_LENGTH = 3250;
    public static String SERVER_ADDR = "https://handy-empire-131521.appspot.com";
    private static final String TAG = "CareGiverActivity";

    // Changed sender id
    private static final String SENDER_ID = "1059275309009";
    public static String mRegistrationID;

    private static final String ACCNT_KEY = "account key";
    private static final String EMAIL_KEY = "email key";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new GcmRegistrationAsyncTask(this).execute();

        SharedPreferences preferences = getSharedPreferences(getString(R.string.profile_preference), 0);
        if (!preferences.getString(EMAIL_KEY,"").equals("")){
            if (preferences.getString(ACCNT_KEY,"").equals("care recipient")){
                Intent newMedication = new Intent(getApplicationContext(), CareRecipientActivity.class);
                startActivity(newMedication);
                finish();
            } else if (preferences.getString(ACCNT_KEY,"").equals("caregiver")){
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
                        .setRootUrl(SERVER_ADDR + "/_ah/api/");

                regService = builder.build();
            }

            String msg = "";
            try {
                if (gcm == null) {
                    gcm = GoogleCloudMessaging.getInstance(context);
                }
                mRegistrationID = gcm.register(SENDER_ID);
                msg = "Device registered, registration ID = " + mRegistrationID;

                // Send registration ID to server over HTTP so it can use GCM/HTTP
                // to send messages to the app.
                regService.register(mRegistrationID).execute();

            } catch (IOException ex) {
                ex.printStackTrace();
                Log.d(TAG, "Error: " + ex.getMessage());
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

}
