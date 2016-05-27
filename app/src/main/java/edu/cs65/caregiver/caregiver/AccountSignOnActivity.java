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
    private static final String RECIPIENT_NAME = "recipient name";
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

        searchAccount();
    }

    // confirm that your name is correct
    public void onNameConfirm(View v){
        SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.profile_preference), MODE_PRIVATE).edit();
        editor.clear();
        editor.putString(ACCNT_KEY, "care recipient");
        editor.putString(RECIPIENT_NAME, careRecipient);
        editor.apply();

        // connect to patient status page
        Intent intent = new Intent(getApplicationContext(), CareRecipientActivity.class);
        startActivity(intent);
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

    private void searchAccount(){
        // search the database for the name that matches input caregiver
        careRecipient = "Sean Oh";
        setContentView(R.layout.activity_new_carerecipient_search);
        TextView nameSearch = (TextView)findViewById(R.id.carerecipient_name_search);
        nameSearch.setText(careRecipient);

        history = 2;

        // TODO -- should have some account management activity

        // dummy information below
        Log.d(TAG, "executing account post");
        new AsyncTask<Void,Void,Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Gson gson = new Gson();

                HashMap<String, String> account_params = new HashMap<>();
                account_params.put("email", careGiver);
                account_params.put("password","default");
                account_params.put("registrationId", registrationID);

                try {
                    String response = ServerUtilities.post(SERVER_ADDR + "/login_caregiver.do", account_params);
                    Log.d(TAG, "post response: " + response);
                    if (response.equals("")){
                        runOnUiThread(new Runnable() {
                          public void run() {
                            Toast.makeText(mContext, "Email does not exist in our system", Toast.LENGTH_SHORT).show();
                          }
                        });
                    } else{
//                        String storedPackage = gson.toJson(response);
//                        CareGiver loaded_data = gson.fromJson(response, CareGiver.class);

                        System.out.println("package " + response);
                    }
                } catch (IOException e) {
                    Log.d(TAG, "failed to issue post - Error msg: " + e.getMessage());
                    e.printStackTrace();

                }
                return null;
            }

        }.execute();
    }

}
