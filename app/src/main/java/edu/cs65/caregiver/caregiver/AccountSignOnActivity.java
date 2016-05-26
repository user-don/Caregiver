package edu.cs65.caregiver.caregiver;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class AccountSignOnActivity extends Activity {

    private String careGiver;
    private String careRecipient;
    private int history;

    private static final String ACCNT_KEY = "account key";
    private static final String RECIPIENT_NAME = "recipient name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_account);
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

        // search the database for the name that matches inputed caregiver
        careRecipient = "Sean Oh";
        setContentView(R.layout.activity_new_carerecipient_search);
        TextView nameSearch = (TextView)findViewById(R.id.carerecipient_name_search);
        nameSearch.setText(careRecipient);

        history = 2;
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

}
