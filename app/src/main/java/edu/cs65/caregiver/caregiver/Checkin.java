package edu.cs65.caregiver.caregiver;

import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.view.View;


public class Checkin extends AppCompatActivity {

    private Vibrator v;

    @Override
    public void onCreate(Bundle SavedInstanceState) {
        super.onCreate(SavedInstanceState);
        setContentView(R.layout.activity_checkin);
        startVibration();
    }

    public void onCheckInClicked(View v) {
        // send message to caregiver
        stopVibration();
        finish();
    }

    public void startVibration() {
        v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {0, 500, 1000};
        v.vibrate(pattern, 0);
    }

    public void stopVibration() {
        v.cancel();
    }

}
