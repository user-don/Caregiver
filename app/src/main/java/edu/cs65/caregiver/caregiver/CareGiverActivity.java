package edu.cs65.caregiver.caregiver;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import java.sql.Time;

import edu.cs65.caregiver.caregiver.model.CareGiver;
import edu.cs65.caregiver.caregiver.model.MedicationAlert;
import edu.cs65.caregiver.caregiver.model.Recipient;

public class CareGiverActivity extends AppCompatActivity {

    public static final int  NEW_MEDICATION_REQUEST = 1;
    public static final String ADDED_MEDICATION_ALERT = "new alert";

    private CareGiver mCareGiver;
    private Recipient mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_care_giver);

        /* TODO -> CONNECT WITH BACKEND AND REQUEST ALL MEDICATIONS FOR CAREGIVER'S RECIPIENT */

    }

    public void onClickNewMedication(View v) {
        Intent i = new Intent(this, NewMedicationActivity.class);
        startActivityForResult(i, NEW_MEDICATION_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == NEW_MEDICATION_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {

                // parse through result for new alert data
                Time time = new Time(data.getLongExtra(NewMedicationActivity.ALERT_TIME, 0));
                MedicationAlert newAlert =
                        new MedicationAlert(data.getStringExtra(NewMedicationActivity.ALERT_NAME),
                                time,
                                data.getIntArrayExtra(NewMedicationActivity.ALERT_DAYS_OF_WEEK),
                                data.getStringArrayListExtra(NewMedicationActivity.ALERT_MEDICATION));

                mReceiver.mAlerts.add(newAlert);
            }
        }

    }
}
