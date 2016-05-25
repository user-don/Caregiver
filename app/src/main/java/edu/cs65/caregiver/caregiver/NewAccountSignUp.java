package edu.cs65.caregiver.caregiver;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by seanoh95 on 5/20/16.
 */
public class NewAccountSignUp extends Activity {

    private String username;
    private String password;
    private EditText userText;
    private EditText passText;
    private String careRecipient;
    private boolean UIswitch;
    private boolean valid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_sign_on);

        userText = (EditText)findViewById(R.id.username);
        passText = (EditText)findViewById(R.id.password);

        valid = false;

    }

    // once a new caregiver is created, take them to the medication page
    public void onCareGiverNext(View v){
        EditText careRecipientInput = (EditText)findViewById(R.id.new_caregiver_recipient);
        careRecipient = careRecipientInput.getText().toString();

        //add password and username and careRecipient to the database

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
                Intent signUpIntent = new Intent(getApplicationContext(), CareGiverActivity.class);
                signUpIntent.putExtra("username",username);
                signUpIntent.putExtra("password",password);

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
            valid = true;

            //if available, start signup activity else display toast saying to choose a new username
            if (valid) {
                setContentView(R.layout.activity_new_caregiver);
            } else {
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
}