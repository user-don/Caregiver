package edu.cs65.caregiver.caregiver;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

import java.io.IOException;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import edu.cs65.caregiver.caregiver.model.RecipientToCareGiverMessage;

/**
 * Created by ellenli on 5/19/16.
 */
public class FallActivity extends Activity {

    private Vibrator v;
    private static final String SERVER_ADDR = "https://handy-empire-131521.appspot.com";
    private String mRegistration;
    private String mEmail;
    private Timer mTimer;
    private int countdownTime = 60000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fall);

        // get registrationID and email for sending messages
        Intent i = getIntent();
        mRegistration = i.getStringExtra("registration");
        mEmail = i.getStringExtra("email");

        // start vibration
        startVibration();

        // stop vibration, cancel time, and close activity when "I AM OK" is pressed
        Button closeButton = (Button) findViewById(R.id.FallBtnOk);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTimer.cancel();
                mTimer.purge();
                SensorService.fallDetected = false;
                stopVibration();
                finish();
            }
        });

        // Display timer countdown
        CountDownTimer countDownTimer = new CountDownTimer(countdownTime,1000) {
            private boolean warned = false;
            @Override
            public void onTick(long millisUntilFinished) {
                // Getting reference to the TextView tv_counter of the layout activity_main
                TextView tvCounter = (TextView) findViewById(R.id.fallTimer);

                Long timeLeft = (millisUntilFinished / 1000);
                // Updating the TextView
                tvCounter.setText( Long.toString(timeLeft));
            }

            @Override
            public void onFinish() {}
        }.start();

        // Send HELP message when button is clicked
        final Button helpButton = (Button) findViewById(R.id.FallBtnHelp);
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Stop timer
                mTimer.cancel();
                mTimer.purge();

                // Send help message
                new AsyncTask<Void, Void, Void>() {
                    private static final String TAG = "Notify HELP AT";

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

                            backend.sendNotificationToCaregiver(mRegistration, mEmail, msg.selfToString()).execute();
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

                // stop vibration and finish activity
                SensorService.fallDetected = false;
                stopVibration();
                finish();
            }
        });

        // Timer
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {

                new AsyncTask<Void, Void, Void>() {
                    private static final String TAG = "Notify HELP AT";

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

                            backend.sendNotificationToCaregiver(mRegistration, mEmail, msg.selfToString()).execute();
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
                        SensorService.fallDetected = false;
                        stopVibration();
                        finish();
                    }
                }.execute();

            }
        }, countdownTime);
    }

    // start phone vibrations
    public void startVibration() {
        v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {0, 500, 1000};
        v.vibrate(pattern, 0);
    }

    // stop phone vibrations
    public void stopVibration() {
        v.cancel();
    }

}
