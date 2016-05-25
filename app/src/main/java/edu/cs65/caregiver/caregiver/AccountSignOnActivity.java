package edu.cs65.caregiver.caregiver;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class AccountSignOnActivity extends Activity {

    private String username;
    private String password;
    private boolean valid;
    private int accntType;
    private String careRecipient;
    private String careGiver;
    private Thread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_account);
    }

    // if the new user says that they are a care giver
    public void onCareGiverClick(View v){
        Intent signUpIntent = new Intent(getApplicationContext(), NewAccountSignUp.class);
        startActivity(signUpIntent);
    }
    // if the new user says that they are a care recipient
    public void onCareRecipientClick(View v){
        setContentView(R.layout.activity_new_carerecipient);
    }

    // as a new care recipient, search for your care giver
    public void onSearch(View v){
        EditText careGiverInput = (EditText)findViewById(R.id.find_caregiver);
        careGiver = careGiverInput.getText().toString();

        // search the database for the name that matches inputed caregiver
        String careRecipient = "Sean Oh";
        setContentView(R.layout.activity_new_carerecipient_search);
        TextView nameSearch = (TextView)findViewById(R.id.carerecipient_name_search);
        nameSearch.setText(careRecipient);
    }

    // confirm that your name is correct
    public void onNameConfirm(View v){
        // connect to patient status page
        Intent intent = new Intent(getApplicationContext(), CareRecipientActivity.class);
        startActivity(intent);
    }

    // wrong name appears
    public void onNameReject(View v){
        setContentView(R.layout.activity_new_carerecipient);
    }

}
