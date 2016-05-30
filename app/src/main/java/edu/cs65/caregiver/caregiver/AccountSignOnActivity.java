package edu.cs65.caregiver.caregiver;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;

import edu.cs65.caregiver.caregiver.model.CareGiver;

public class AccountSignOnActivity extends Activity {

    private String careGiver;
    private String careRecipient;
    private String registrationID;
    private int history;
    private Context mContext;

    private static final String TAG = "CareGiverActivity";
    private static final String ACCNT_KEY = "account key";
    private static final String EMAIL_KEY = "email key";
    private static final String REGISTRATION_KEY = "registration key";
    private static final String RECIPIENT_NAME_KEY = "recipient name";
    private static final String SERVER_ADDR = "https://handy-empire-131521.appspot.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_account);
        mContext = getApplicationContext();

        registrationID = MainActivity.mRegistrationID;
        history = 0;
    }

    // if the new user says that they are a care giver
    public void onCareGiverClick(View v){
        Intent signUpIntent = new Intent(getApplicationContext(), NewAccountSignUp.class);
        startActivity(signUpIntent);
        finish();
    }
    // if the new user says that they are a care recipient
    public void onCareRecipientClick(View v){
        setContentView(R.layout.activity_new_carerecipient);
        history = 1;
    }

    // as a new care recipient, search for your care giver
    public void onSearch(View v){
        EditText careGiverInput = (EditText)findViewById(R.id.find_caregiver);
        careGiver = careGiverInput.getText().toString();
        careGiver = careGiver.toLowerCase();

        searchAccount();
    }

    // confirm that your name is correct
    public void onNameConfirm(View v){
        SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.profile_preference), MODE_PRIVATE).edit();
        editor.clear();
        editor.putString(ACCNT_KEY, "care recipient");
        editor.putString(EMAIL_KEY, careGiver);
        editor.putString(RECIPIENT_NAME_KEY, careRecipient);
        editor.putString(REGISTRATION_KEY, registrationID);
        editor.apply();

        createCarerecipientAccount();
    }

    // wrong name appears
    public void onNameReject(View v){
        setContentView(R.layout.activity_new_carerecipient);
    }

    @Override
    public void onBackPressed() {
        switch(history){
            case 0:
                super.onBackPressed();
                break;
            case 1:
                setContentView(R.layout.activity_new_account);
                history = 0;
                break;
            case 2:
                setContentView(R.layout.activity_new_carerecipient);
                history = 1;
                break;
            default:
                super.onBackPressed();
        }
    }

    public void createCarerecipientAccount() {

        // dummy information below
        Log.d(TAG, "executing recipient creation");
        new AsyncTask<Void,Void,Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                edu.cs65.caregiver.backend.messaging.Messaging.Builder builder =
                        new edu.cs65.caregiver.backend.messaging.Messaging
                                .Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null)
                                .setRootUrl(SERVER_ADDR + "/_ah/api/");

                edu.cs65.caregiver.backend.messaging.Messaging backend = builder.build();

                try {
                    backend.registerPatientAccount(careGiver, registrationID).execute();

                    runOnUiThread(new Runnable() {
                        public void run() {
                        // connect to patient status page
                        Intent intent = new Intent(getApplicationContext(), CareRecipientActivity.class);
                        startActivity(intent);
                            finish();
                        }
                    });
                } catch (IOException e) {
                    Log.d(TAG, "failed to register recipient account - Error msg: " + e.getMessage());
                    e.printStackTrace();
                }
                return null;
            }

        }.execute();
    }

    private void searchAccount(){
        // dummy information below
        Log.d(TAG, "executing account post");
        new AsyncTask<Void,Void,Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Gson gson = new Gson();

                edu.cs65.caregiver.backend.messaging.Messaging.Builder builder =
                        new edu.cs65.caregiver.backend.messaging.Messaging
                                .Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null)
                                .setRootUrl(SERVER_ADDR + "/_ah/api/");

                edu.cs65.caregiver.backend.messaging.Messaging backend = builder.build();
                edu.cs65.caregiver.backend.messaging.model.CaregiverEndpointsObject response = null;

                try {
                    response = backend.getAccountInfo(careGiver).execute();
                    String storedData = response.getData();
                    CareGiver loaded_data = gson.fromJson(storedData, CareGiver.class);

                    // search the database for the name that matches input caregiver
                    careRecipient = loaded_data.mRecipients.get(0).mName;
                    runOnUiThread(new Runnable() {
                        public void run() {
                            setContentView(R.layout.activity_new_carerecipient_search);
                            TextView nameSearch = (TextView) findViewById(R.id.carerecipient_name_search);
                            nameSearch.setText(careRecipient);
                        }
                    });

                    history = 2;
                } catch (IOException e) {
                    Log.d(TAG, "failed to issue post - Error msg: " + e.getMessage());
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Incorrect caregiver name", Toast.LENGTH_LONG).show();
                        }
                    });
                }
                return null;
            }

        }.execute();
    }

}
