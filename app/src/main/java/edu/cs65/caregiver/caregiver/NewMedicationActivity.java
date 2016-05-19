package edu.cs65.caregiver.caregiver;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.content.DialogInterface;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;

import edu.cs65.caregiver.caregiver.model.MedicationAlert;

public class NewMedicationActivity extends AppCompatActivity {

    private static final String TAG = "new medication activity";

    private String mAlertName;
    private Time mAlertTime;
    private int mRecurrenceType = 0;
    private int[] mDaysOfWeek = new int[7];
    private ArrayList<String> mMedications;

    public final static String ALERT_NAME = "alert_name";
    public final static String ALERT_TIME = "alert_time";
    public final static String ALERT_RECURRENCE_TYPE = "recurrence_type";
    public final static String ALERT_DAYS_OF_WEEK = "days_of_week";
    public final static String ALERT_MEDICATION = "medications";

    /* ----------------------------- Life cycle functions ----------------------------- */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_medication);

        Intent i = getIntent();

        // if saved instance state is not null, load that information
        if (savedInstanceState != null) {

            mAlertName = savedInstanceState.getString(ALERT_NAME, null);

            long time = savedInstanceState.getLong(ALERT_TIME, -1);
            if (time > 0) {
                mAlertTime = new Time(time);
            }

            mRecurrenceType = savedInstanceState.getInt(ALERT_RECURRENCE_TYPE, 0);
            mDaysOfWeek = savedInstanceState.getIntArray(ALERT_DAYS_OF_WEEK);
            mMedications = savedInstanceState.getStringArrayList(ALERT_MEDICATION);

        // else if the intent has existing information, pull that up
        } else if (i.getStringExtra(ALERT_NAME) != null) {

            mAlertName = i.getStringExtra(ALERT_NAME);
            mAlertTime = i.getParcelableExtra(ALERT_TIME);
            mRecurrenceType = i.getIntExtra(ALERT_RECURRENCE_TYPE, 0);
            mDaysOfWeek = i.getIntArrayExtra(ALERT_DAYS_OF_WEEK);
            mMedications = i.getStringArrayListExtra(ALERT_MEDICATION);

        // else initialize data
        } else {
            mMedications = new ArrayList<>();
        }

        setSpinnerAdapter();
        setMedicationAdapter();
        updateUI();
    }

    @Override
    public void onSaveInstanceState(Bundle outstate) {
        super.onSaveInstanceState(outstate);

        // need to save current information
        outstate.putString(ALERT_NAME, mAlertName);

        if (mAlertTime != null) {
            outstate.putLong(ALERT_TIME, mAlertTime.getTime());
        }

        outstate.putInt(ALERT_RECURRENCE_TYPE, mRecurrenceType);
        outstate.putIntArray(ALERT_DAYS_OF_WEEK, mDaysOfWeek);
        outstate.putStringArrayList(ALERT_MEDICATION, mMedications);
    }

    /* ----------------------------- UI callbacks ----------------------------- */

    public void onClickSetTime(View v) {
        TimePickerDialog.OnTimeSetListener time_listener =
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        setTime(hourOfDay, minute);
                    }
                };
        Calendar calendar = Calendar.getInstance();
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        new TimePickerDialog(this, time_listener, hourOfDay, minute, false).show();
    }

    public void setTime(int hourOfDay, int minute) {
        mAlertTime = new Time(hourOfDay, minute, 0);
        updateUI();
    }

    public void onClickAddMedication(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText("", TextView.BufferType.EDITABLE);
        input.setHint("Enter Medication Name");

        builder.setView(input);
        builder.setTitle("Add Medication to Group");
        builder.setPositiveButton("ADD", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {

                if (!input.getText().toString().equals("")) {
                    addMedication(input.getText().toString());
                } else {
                    Toast.makeText(NewMedicationActivity.this, "Entry Failed -- Please enter a name",
                            Toast.LENGTH_SHORT).show();
                }

            }
        });

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.create().show();
    }

    public void onClickSave(View v) {

        EditText name_text = (EditText)findViewById(R.id.alert_name);
        mAlertName = name_text.getText().toString();

        // save all information to and update account
        if (checkFields() == false) {
            return;
        }

        Toast.makeText(this, "Added Medication Alert", Toast.LENGTH_SHORT).show();

        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_OK, resultIntent);

        // need to save current information
        resultIntent.putExtra(ALERT_NAME, mAlertName);

        if (mAlertTime != null) {
            resultIntent.putExtra(ALERT_TIME, mAlertTime.getTime());
        }
        //resultIntent.putExtra(ALERT_RECURRENCE_TYPE, mRecurrenceType);
        resultIntent.putExtra(ALERT_DAYS_OF_WEEK, mDaysOfWeek);
        resultIntent.putStringArrayListExtra(ALERT_MEDICATION, mMedications);

        finish();
    }

    public boolean checkFields() {

        if (mAlertName == null || mAlertName.equals("")) {
            Toast.makeText(this,"Please Enter Alert Name", Toast.LENGTH_SHORT).show();
            return false;
        } else if (mAlertTime == null) {
            Toast.makeText(this,"Please Enter Alert Time", Toast.LENGTH_SHORT).show();
            return false;
        } else if (checkRecurrences() == 0) {
            Toast.makeText(this,"Please Set Alert For At Least One Day In Week", Toast.LENGTH_SHORT).show();
            return false;
        } else if (mMedications.size() == 0) {
            Toast.makeText(this,"Please Add A Medication", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    public void onClickCancel(View v) {
        finish();
    }

    public void updateUI() {

        EditText alert_name = (EditText) findViewById(R.id.alert_name);
        if (mAlertName != null) {
            mAlertName = alert_name.getText().toString();
            alert_name.setText(mAlertName);
        }

        TextView alert_time = (TextView) findViewById(R.id.alert_time);
        if (mAlertTime != null) {
            alert_time.setText(mAlertTime.toString());
        }

        Spinner recurrence = (Spinner) findViewById(R.id.recurrence_spinner);
        recurrence.setSelection(mRecurrenceType);

        if (mRecurrenceType == 1) {
            enableAllDays();
        } else {
            disableAllDays();
        }

    }

    public void setSpinnerAdapter() {

        Spinner recurrence_spinner = (Spinner) findViewById(R.id.recurrence_spinner);
        if (recurrence_spinner != null) {
            recurrence_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    mRecurrenceType = pos;
                    if (pos == 1) {
                        enableAllDays();
                    } else {
                        disableAllDays();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                }
            });
        }
    }

    public void setMedicationAdapter() {
        // set array adapter for listview of medication
        final ListView medications = (ListView) findViewById(R.id.medication_list);

        // TODO -- set empty entry text; not working right now
        /// View empty = getLayoutInflater().inflate(R.layout.empty_list_view, null, false);
//        addContentView(empty, new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
//                ActionBar.LayoutParams.MATCH_PARENT));
        //((ViewGroup)medications.getParent()).addView(empty);
        //medications.setEmptyView(empty);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mMedications);
        medications.setAdapter(adapter);

        medications.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView tv = (TextView) view;
                displayDeleteMedicationDialog(tv.getText().toString(), position);
            }
        });
    }

    public void displayDeleteMedicationDialog(String name, final int index) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Delete Medication: " + name + "?");
        builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                mMedications.remove(index);

                // update listview with new information
                ListView medications = (ListView) findViewById(R.id.medication_list);
                ArrayAdapter list_adapter = (ArrayAdapter)medications.getAdapter();
                list_adapter.notifyDataSetChanged();
            }
        });

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.create().show();
    }

    int checkRecurrences() {

        int checked_days = 0;

        if (isChecked(R.id.chkbx_monday)) {
            mDaysOfWeek[0] = 1;
            checked_days++;
        } else
            mDaysOfWeek[0] = 0;

        if (isChecked(R.id.chkbx_tuesday)){
            mDaysOfWeek[1] = 1;
            checked_days++;
        } else
            mDaysOfWeek[1] = 0;

        if (isChecked(R.id.chkbx_wednesday)){
            mDaysOfWeek[2] = 1;
            checked_days++;
        } else
            mDaysOfWeek[2] = 0;

        if (isChecked(R.id.chkbx_thursday)){
            mDaysOfWeek[3] = 1;
            checked_days++;
        } else
            mDaysOfWeek[3] = 0;

        if (isChecked(R.id.chkbx_friday)){
            mDaysOfWeek[4] = 1;
            checked_days++;
        }
        mDaysOfWeek[4] = 0;

        if (isChecked(R.id.chkbx_saturday)){
            mDaysOfWeek[5] = 1;
            checked_days++;
        }
        mDaysOfWeek[5] = 0;

        if (isChecked(R.id.chkbx_sunday)){
            mDaysOfWeek[6] = 1;
            checked_days++;
        }
        mDaysOfWeek[6] = 0;

        return checked_days;
    }

    public boolean isChecked(int id) {
        CheckBox box = (CheckBox) findViewById(id);
        return box.isChecked();
    }

    public void enableAllDays() {
        enableCheckbox(R.id.chkbx_monday);
        enableCheckbox(R.id.chkbx_tuesday);
        enableCheckbox(R.id.chkbx_wednesday);
        enableCheckbox(R.id.chkbx_thursday);
        enableCheckbox(R.id.chkbx_friday);
        enableCheckbox(R.id.chkbx_saturday);
        enableCheckbox(R.id.chkbx_sunday);
    }

    public void disableAllDays() {
        disableCheckbox(R.id.chkbx_monday);
        disableCheckbox(R.id.chkbx_tuesday);
        disableCheckbox(R.id.chkbx_wednesday);
        disableCheckbox(R.id.chkbx_thursday);
        disableCheckbox(R.id.chkbx_friday);
        disableCheckbox(R.id.chkbx_saturday);
        disableCheckbox(R.id.chkbx_sunday);
    }

    public void enableCheckbox(int id) {
        CheckBox box = (CheckBox) findViewById(id);
        if (box != null) {
            box.setChecked(false);
            box.setEnabled(true);
        }
    }

    public void disableCheckbox(int id) {
        CheckBox box = (CheckBox) findViewById(id);
        if (box != null) {
            box.setChecked(true);
            box.setEnabled(false);
        }
    }


    /* ----------------------------- data controlling functions ----------------------------- */

    public void addMedication(String medication) {
        mMedications.add(medication);
    }
}
