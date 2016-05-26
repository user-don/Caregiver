package edu.cs65.caregiver.caregiver;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;

import edu.cs65.caregiver.caregiver.controllers.DataController;
import edu.cs65.caregiver.caregiver.model.CareGiver;

/**
 * Created by seanoh95 on 5/20/16.
 */
public class NewAccountSignUp extends Activity {

    private String username;
    private String password;
    private String registrationID;
    private DataController mDataController;
    private EditText userText;
    private EditText passText;
    private String careRecipient;
    private boolean UIswitch;
    private boolean valid;

    private static final String TAG = "CareGiverActivity";
    private static final String SERVER_ADDR = "https://handy-empire-131521.appspot.com";

    private static final String ACCNT_KEY = "account key";
    private static final String EMAIL_KEY = "email key";
    private static final String PASSWORD_KEY = "password key";
    private static final String CAREGIVER_KEY = "caregiver name";
    private static final String RECIPIENT_NAME = "recipient name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_sign_on);

        userText = (EditText)findViewById(R.id.username);
        passText = (EditText)findViewById(R.id.password);

        valid = false;
        registrationID = MainActivity.mRegistrationID;

        mDataController = DataController.getInstance(getApplicationContext());
        mDataController.initializeData(getApplicationContext());
    }

    // once a new caregiver is created, take them to the medication page
    public void onCareGiverNext(View v){
        EditText careRecipientInput = (EditText)findViewById(R.id.new_caregiver_recipient);
        careRecipient = careRecipientInput.getText().toString();

        //add password and username and careRecipient to the database
        SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.profile_preference), MODE_PRIVATE).edit();
        editor.clear();
        editor.putString(ACCNT_KEY, "caregiver");
        editor.putString(EMAIL_KEY, username);
        editor.putString(PASSWORD_KEY, password);
        editor.putString(RECIPIENT_NAME, careRecipient);
        editor.apply();

        Intent newMedication = new Intent(getApplicationContext(), CareGiverActivity.class);
        startActivity(newMedication);
        finish();
    }

    // If the user would like to sign into an existing account
    public void onLogIn (View v) {
        username = userText.getText().toString();
        password = passText.getText().toString();

        if (username.equals("") || password.equals("")){
            displayToast("Please enter both an email and password");
        } else {
            //check to see if that account is valid
            valid = true;

            //if not, available, display toast saying to put in a valid account
            if (valid) {
                //Get information about the person from login

                Intent signUpIntent = new Intent(getApplicationContext(), CareGiverActivity.class);

                SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.profile_preference), MODE_PRIVATE).edit();
                editor.clear();
                editor.putString(ACCNT_KEY, "caregiver");
                editor.putString(EMAIL_KEY, username);
                editor.putString(PASSWORD_KEY, password);
                editor.apply();

                startActivity(signUpIntent);
            } else {
                displayToast("That account doesn't exist. Please try again");
            }
        }
    }

    // If the user would like to create a new account
    public void onSignUp (View v){
        username = userText.getText().toString();
        password = passText.getText().toString();

        if (username.equals("") || password.equals("")){
            displayToast("Please enter both an email and password");
        } else {
            //check to see if that account is available
            mDataController.careGiver = new CareGiver(username);
            createAccount();
            valid = true;

            //if available, start signup activity else display toast saying to choose a new username
            if (valid) {
                setContentView(R.layout.activity_new_caregiver);
            } else {
                System.out.println("take");
                displayToast("Account taken. Please use a new email.");
            }
        }
    }

    public void displayToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        if (UIswitch){
            setContentView(R.layout.activity_new_account);
            UIswitch = false;
        } else {
            super.onBackPressed();
        }
    }

    public void createAccount() {
        // TODO -- should have some account management activity

        // dummy information below
        Log.d(TAG, "executing account post");
        new AsyncTask<Void,Void,Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Gson gson = new Gson();

                HashMap<String, String> account_params = new HashMap<>();
                account_params.put("email", username);
                account_params.put("password",password);
                account_params.put("registrationId", registrationID);
                account_params.put("caregiver", gson.toJson(mDataController.careGiver));

                try {
                    String response = ServerUtilities.post(SERVER_ADDR + "/create_account.do", account_params);
                    Log.d(TAG, "post response: " + response);
                } catch (IOException e) {
                    Log.d(TAG, "failed to issue post - Error msg: " + e.getMessage());
                    e.printStackTrace();
                }
                return null;
            }

        }.execute();
    }
}