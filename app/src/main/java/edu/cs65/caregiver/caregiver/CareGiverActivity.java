package edu.cs65.caregiver.caregiver;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;

import org.w3c.dom.Text;

import java.sql.Time;
import java.util.ArrayList;

import edu.cs65.caregiver.caregiver.model.CareGiver;
import edu.cs65.caregiver.caregiver.model.MedicationAlert;
import edu.cs65.caregiver.caregiver.model.Recipient;

public class CareGiverActivity extends AppCompatActivity {

    public static final int NEW_MEDICATION_REQUEST = 1;
    public static final int EDIT_MEDICATION_REQUEST = 2;
    public static final String ADDED_MEDICATION_ALERT = "new alert";

    private CareGiver mCareGiver;
    private Recipient mReceiver;

    private static final String CAREGIVER_KEY = "current caregiver";
    private static final String RECIPIENT_NAME_KEY = "recipient";
    private SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_care_giver);

        mPrefs = getPreferences(MODE_PRIVATE);

        /* TODO -> CONNECT WITH BACKEND AND REQUEST ALL MEDICATIONS FOR CAREGIVER'S RECIPIENT */

        loadData();
        if (mCareGiver == null) {
            mCareGiver = new CareGiver("test");
            mReceiver = mCareGiver.addRecipient("test recipient");
        } else {
            mReceiver = mCareGiver.getRecipient("test recipient");
        }

        setAlertAdapter();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.care_giver_activity_menu, menu);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveData();
    }

    public void onClickNewMedication(View v) {
        Intent i = new Intent(this, NewMedicationActivity.class);
        startActivityForResult(i, NEW_MEDICATION_REQUEST);
    }

    public void onClickAccount(MenuItem menuItem) {
        // TODO -- should have some account management activity
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case NEW_MEDICATION_REQUEST:
                // Make sure the request was successful
                if (resultCode == RESULT_OK) {

                    // parse through result for new alert data
                    Time time = new Time(data.getLongExtra(NewMedicationActivity.ALERT_TIME, 0));
                    MedicationAlert newAlert =
                            new MedicationAlert(data.getStringExtra(NewMedicationActivity.ALERT_NAME),
                                    time,
                                    data.getIntArrayExtra(NewMedicationActivity.ALERT_DAYS_OF_WEEK),
                                    data.getStringArrayListExtra(NewMedicationActivity.ALERT_MEDICATION));

                    mReceiver.addAlert(newAlert);
                }
                break;
            case EDIT_MEDICATION_REQUEST:
                if (resultCode == RESULT_OK) {

                    String updated_name = data.getStringExtra(NewMedicationActivity.ALERT_NAME);
                    mReceiver.deleteAlert(updated_name);

                    Time time = new Time(data.getLongExtra(NewMedicationActivity.ALERT_TIME, 0));
                    MedicationAlert newAlert =
                            new MedicationAlert(data.getStringExtra(NewMedicationActivity.ALERT_NAME),
                                    time,
                                    data.getIntArrayExtra(NewMedicationActivity.ALERT_DAYS_OF_WEEK),
                                    data.getStringArrayListExtra(NewMedicationActivity.ALERT_MEDICATION));

                    mReceiver.addAlert(newAlert);
                }
                break;
        }

        updateUI();
    }

    public void updateUI() {
        ListView alertList = (ListView) findViewById(R.id.medication_alert_list2);
        ((ArrayAdapter) alertList.getAdapter()).notifyDataSetChanged();

        TextView recipientText = (TextView) findViewById(R.id.recipient_name);
        recipientText.setText(mReceiver.mName);
    }

    public void saveData() {
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        Gson gson = new Gson();
        String saved_data = gson.toJson(mCareGiver);
        prefsEditor.putString(CAREGIVER_KEY, saved_data);
        prefsEditor.commit();
    }

    public void loadData() {
        Gson gson = new Gson();
        String loaded_data = mPrefs.getString(CAREGIVER_KEY, "");
        if (!loaded_data.equals("")) {
            mCareGiver = gson.fromJson(loaded_data, CareGiver.class);
        }
    }

    public void setAlertAdapter() {
        MedAlertArrayAdapter adapter = new MedAlertArrayAdapter(this, mReceiver.mAlerts);
        ListView alert_list = (ListView) findViewById(R.id.medication_alert_list2);
        alert_list.setAdapter(adapter);

        alert_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                displayEditMedicationAlertDialog(mReceiver.mAlerts.get(position), position);
            }
        });

    }

    public void displayEditMedicationAlertDialog(final MedicationAlert alert, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Do what with the alert: " + alert.mName + "?");

        builder.setPositiveButton("Edit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                Intent i = new Intent(getApplicationContext(), NewMedicationActivity.class);

                i.putExtra(NewMedicationActivity.ALERT_NAME, alert.mName);
                i.putExtra(NewMedicationActivity.ALERT_TIME, alert.mTime);

                // quickly get the recurrence type
                int recurrenceType = 0;     // default to daily
                for (int j = 0; j < 7; j++) {
                    if (alert.mAlertDays[j] == 0) {     // if any day is not checked -> day-wise enabled
                        recurrenceType = 1;
                        break;
                    }
                }
                i.putExtra(NewMedicationActivity.ALERT_RECURRENCE_TYPE, recurrenceType);
                i.putExtra(NewMedicationActivity.ALERT_DAYS_OF_WEEK, alert.mAlertDays);
                i.putExtra(NewMedicationActivity.ALERT_MEDICATION, alert.mMedications);

                long time = alert.mTime.getTime();
                i.putExtra(NewMedicationActivity.ALERT_TIME, time);

                startActivityForResult(i, EDIT_MEDICATION_REQUEST);
            }
        });

        builder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                mReceiver.mAlerts.remove(position);
                updateUI();
            }
        });

        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                arg0.cancel();
            }
        });

        builder.create().show();
    }

    public class MedAlertArrayAdapter extends ArrayAdapter<MedicationAlert> {
        private final Context context;
        private final ArrayList<MedicationAlert> alerts;

        public MedAlertArrayAdapter(Context context, ArrayList<MedicationAlert> alerts) {
            super(context, R.layout.medication_alert, alerts);
            this.context = context;
            this.alerts = alerts;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            MedicationAlert alert = alerts.get(position);

            View rowView = inflater.inflate(R.layout.medication_alert, parent, false);

            TextView title = (TextView) rowView.findViewById(R.id.med_alert_title);
            title.setText(alert.mName + ": " + alert.mTime.toString());

            TextView details = (TextView) rowView.findViewById(R.id.med_alert_detail);
            Log.d("here", getDayString(alert.mAlertDays));
            details.setText(getDayString(alert.mAlertDays));

            ImageView image = (ImageView) rowView.findViewById(R.id.med_alert_image);
            image.setImageResource(R.drawable.ic_done_black_24dp);

            return rowView;
        }
    }

    public String getDayString(int[] arr) {
        StringBuilder sb = new StringBuilder(100);

        int days = 0;
        if (arr[0] == 1) {
            sb.append("Mon ");
            days++;
        }
        if (arr[1] == 1) {
            sb.append("Tue ");
            days++;
        }
        if (arr[2] == 1) {
            sb.append("Wed ");
            days++;
        }
        if (arr[3] == 1) {
            sb.append("Thu ");
            days++;
        }
        if (arr[4] == 1) {
            sb.append("Fri ");
            days++;
        }
        if (arr[5] == 1) {
            sb.append("Sat ");
            days++;
        }
        if (arr[6] == 1) {
            sb.append("Sun ");
            days++;
        }

        if (days == 7)
            return "Daily";
        else
            return sb.toString();

    }
}
