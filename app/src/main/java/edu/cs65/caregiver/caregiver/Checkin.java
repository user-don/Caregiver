package edu.cs65.caregiver.caregiver;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

import java.io.IOException;
import java.util.Calendar;

import edu.cs65.caregiver.caregiver.model.RecipientToCareGiverMessage;


public class Checkin extends AppCompatActivity {

    private Vibrator v;
    private static final String SERVER_ADDR = "https://handy-empire-131521.appspot.com";
    private String mRegistration;
    private String mEmail;

    @Override
    public void onCreate(Bundle SavedInstanceState) {
        super.onCreate(SavedInstanceState);

        Intent i = getIntent();
        mRegistration = i.getStringExtra("registration");
        mEmail = i.getStringExtra("email");

        setContentView(R.layout.activity_checkin);
        startVibration();
    }

    public void onCheckInClicked(View v) {
        // TODO -- send message to caregiver

        new AsyncTask<Void, Void, Void>() {
            private static final String TAG = "check in AT";

            @Override
            protected Void doInBackground(Void... params) {

                edu.cs65.caregiver.backend.messaging.Messaging.Builder builder =
                        new edu.cs65.caregiver.backend.messaging.Messaging
                                .Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null)
                                .setRootUrl(SERVER_ADDR + "/_ah/api/");

                edu.cs65.caregiver.backend.messaging.Messaging backend = builder.build();

                Log.d(TAG, "Notifying caregiver of help: " + mEmail);
                try {

                    // make help message
                    RecipientToCareGiverMessage msg =
                            new RecipientToCareGiverMessage(RecipientToCareGiverMessage.HELP,
                                    null,
                                    Calendar.getInstance().getTime().getTime());

                    backend.sendNotificationToCaregiver(mRegistration, mEmail, msg.selfToString());
                    backend.getAccountInfo(mEmail).execute();
                } catch (IOException e) {
                    Log.d(TAG, "send help failed");
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                Log.d(TAG,"sent help message\n");
                Toast.makeText(getApplicationContext(), "CAREGIVER HAS BEEN ALERTED",
                        Toast.LENGTH_LONG).show();
            }
        }.execute();

        stopVibration();
        finish();
    }

    public void startVibration() {
        v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {0, 500, 1000};
        v.vibrate(pattern, 0);
    }

    public void stopVibration() {
        v.cancel();
    }

}
