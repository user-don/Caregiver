package edu.cs65.caregiver.caregiver;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ellenli on 5/19/16.
 */
public class FallActivity extends Activity {

    private static FallActivity parent;

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
                // send message to caregiver

                Toast.makeText(getApplicationContext(), "HELP REQUESTED",
                        Toast.LENGTH_LONG).show();
                finish();
            }
        });

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {



//                Toast.makeText(getParent().getBaseContext(), "HELP REQUESTED",
//                        Toast.LENGTH_LONG).show();
                finish();
            }
        }, 6000);

    }
}
