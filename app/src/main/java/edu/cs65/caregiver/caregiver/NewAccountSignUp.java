package edu.cs65.caregiver.caregiver;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by seanoh95 on 5/20/16.
 */
public class NewAccountSignUp extends Activity {

    private Bundle extras;
    private String username;
    private String password;
    private String careRecipient;
    private String careGiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_account);

        extras = getIntent().getExtras();
        username = extras.getString("username");
        password = extras.getString("password");

    }

    // if the new user says that they are a care giver
    public void onCareGiverClick(View v){
        setContentView(R.layout.activity_new_caregiver);
    }

    // if the new user says that they are a care recipient
    public void onCareRecipientClick(View v){
        setContentView(R.layout.activity_new_carerecipient);
    }

    // once a new caregiver is created, take them to the medication page
    public void onCareGiverNext(View v){
        EditText careRecipientInput = (EditText)findViewById(R.id.new_caregiver_recipient);
        careRecipient = careRecipientInput.getText().toString();
        //add password and username and careRecipient to the database

        Intent newMedication = new Intent(getApplicationContext(), NewMedicationActivity.class);
        startActivity(newMedication);
        finish();
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
    }

    // wrong name appears
    public void onNameReject(View v){
        setContentView(R.layout.activity_new_carerecipient);
    }
}
