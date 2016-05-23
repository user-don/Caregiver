package edu.cs65.caregiver.caregiver;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
public class AccountSignOnActivity extends Activity {

    private EditText username;
    private EditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_sign_on);

        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
    }

    // If the user would like to sign into an existing account
    public void onLogIn (View v) {

    }

    // If the user would like to create a new account
    public void onSignUp (View v){
        Intent signUpIntent = new Intent(getApplicationContext(), NewAccountSignUp.class);
        signUpIntent.putExtra("username",username.getText().toString());
        signUpIntent.putExtra("password",password.getText().toString());
        startActivity(signUpIntent);
    }
}
