package edu.cs65.caregiver.caregiver;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by ellenli on 5/19/16.
 */
public class FallActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fall);

        Button closeButton = (Button) findViewById(R.id.FallBtnOk);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button helpButton = (Button) findViewById(R.id.FallBtnHelp);
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "HELP REQUESTED",
                        Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }
}
