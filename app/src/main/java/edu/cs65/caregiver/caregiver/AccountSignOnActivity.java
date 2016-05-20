package edu.cs65.caregiver.caregiver;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class AccountSignOnActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_sign_on);
    }

    public void onCareReceiverClicked(View v) {
        Intent intent = new Intent(AccountSignOnActivity.this, CareRecipientActivity.class);
        AccountSignOnActivity.this.startActivity(intent);

    }


}
