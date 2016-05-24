package edu.cs65.caregiver.caregiver;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class AccountSignOnActivity extends Activity {

    private EditText userText;
    private EditText passText;
    private String username;
    private String password;
    private boolean valid;
    private int accntType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_sign_on);

        userText = (EditText) findViewById(R.id.username);
        passText = (EditText) findViewById(R.id.password);

        valid = false;
        accntType = -1;
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
            accntType = 0;

            //if not, available, display toast saying to put in a valid account
            if (valid) {
                if (accntType == 0){
                    Intent signUpIntent = new Intent(getApplicationContext(), CareGiverActivity.class);
                    signUpIntent.putExtra("username",username);
                    signUpIntent.putExtra("password",password);

                    startActivity(signUpIntent);
                } else if (accntType == 1){
                    Intent signUpIntent = new Intent(getApplicationContext(), CareRecipientActivity.class);
                    signUpIntent.putExtra("username",username);
                    signUpIntent.putExtra("password",password);

                    startActivity(signUpIntent);
                }
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
                Intent signUpIntent = new Intent(getApplicationContext(), NewAccountSignUp.class);
                signUpIntent.putExtra("username",username);
                signUpIntent.putExtra("password",password);

                startActivity(signUpIntent);
            } else {
                displayToast("Account taken. Please use a new email.");
            }
        }
    }

    public void displayToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}
