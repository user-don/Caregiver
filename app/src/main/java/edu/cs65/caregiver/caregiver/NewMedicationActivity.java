package edu.cs65.caregiver.caregiver;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.content.DialogInterface;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import edu.cs65.caregiver.caregiver.controllers.DataController;

public class NewMedicationActivity extends AppCompatActivity {
    @BindView(R.id.alert_time) TextView alert_time;
    @BindView(R.id.alert_name) EditText alert_name_et;
    @BindView(R.id.new_medication) EditText new_medication;
    @BindView(R.id.plus_button) ImageView plus_button;

    private static final String TAG = "new medication activity";

    private String mAlertName;
    private Time mAlertTime;
    private boolean mMedsTaken;
    private int mRecurrenceType = 0;
    private int[] mDaysOfWeek = new int[7];
    private ArrayList<String> mMedications;
    private String oldAlertName = "";

    public static String SERVER_ADDR = "https://handy-empire-131521.appspot.com";

    public final static String ALERT_NAME = "alert_name";
    public final static String ALERT_TIME = "alert_time";
    public final static String ALERT_RECURRENCE_TYPE = "recurrence_type";
    public final static String ALERT_DAYS_OF_WEEK = "days_of_week";
    public final static String ALERT_MEDICATION = "medications";
    public final static String ALERT_MEDS_TAKEN = "medsTaken";
    public final static String OLD_ALERT_NAME = "OLD_ALERT_NAME";

    private static DataController mDC;

    /* ----------------------------- Life cycle functions ----------------------------- */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_medication);
        setupUI(findViewById(R.id.newMedPage));
        ButterKnife.bind(this);
        mDC = DataController.getInstance(getApplicationContext());

        // Set desired EditText blinking cursor behavior for alert name
        // See SO Post: http://bit.ly/1Z6bcIo
        alert_name_et.setOnClickListener(alertNameClickListener);
        alert_name_et.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                alert_name_et.setCursorVisible(false);
                if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    in.hideSoftInputFromWindow(alert_name_et.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
                return false;
            }
        });

        //plus_button.setOnClickListener(plusButtonClickListener);
        new_medication.setOnClickListener(newMedicationClickListener);

        Intent i = getIntent();

        // if saved instance state is not null, load that information
        if (i.getStringExtra(ALERT_NAME) != null && savedInstanceState == null) {
            Log.d(TAG, "loading edit entry");

            mAlertName = i.getStringExtra(ALERT_NAME);
            oldAlertName = mAlertName;
            alert_name_et.setText(mAlertName);
            long time = i.getLongExtra(ALERT_TIME, 0);
            mAlertTime = new Time(time);
            mRecurrenceType = i.getIntExtra(ALERT_RECURRENCE_TYPE, 0);
            Spinner spinner = (Spinner) findViewById(R.id.recurrence_spinner);
            spinner.setSelection(mRecurrenceType);

            mDaysOfWeek = i.getIntArrayExtra(ALERT_DAYS_OF_WEEK);
            mMedications = i.getStringArrayListExtra(ALERT_MEDICATION);
            mMedsTaken = false;

            // else initialize data
        } else if (savedInstanceState != null) {
            Log.d(TAG, "loading saved instance");

            mAlertName = savedInstanceState.getString(ALERT_NAME, null);
            alert_name_et.setText(mAlertName);

            long time = savedInstanceState.getLong(ALERT_TIME, -1);
            if (time > 0) {
                mAlertTime = new Time(time);
            }

            mRecurrenceType = savedInstanceState.getInt(ALERT_RECURRENCE_TYPE, 0);
            mDaysOfWeek = savedInstanceState.getIntArray(ALERT_DAYS_OF_WEEK);
            mMedications = savedInstanceState.getStringArrayList(ALERT_MEDICATION);
            mMedsTaken = savedInstanceState.getBoolean(ALERT_MEDS_TAKEN);

        // else if the intent has existing information, pull that up
        } else {
            Log.d(TAG, "loading new instance");
            mMedications = new ArrayList<>();
            checkMedList();
        }

        setSpinnerAdapter();
        setMedicationAdapter();

        updateUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.new_medication_menu, menu);

        return true;
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
        outstate.putBoolean(ALERT_MEDS_TAKEN, mMedsTaken);
        outstate.putStringArrayList(ALERT_MEDICATION, mMedications);
    }

    /* ----------------------------- UI callbacks ----------------------------- */

    // displays time picker for new mdication
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

    // adds a medication to medList
    public void onClickAddMedication(View v) {
        // Hide cursor on new_medication EditText and hide the keyboard
        new_medication.setCursorVisible(false);
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);

        String medication = new_medication.getText().toString();
        if (!medication.equals("")){
            if (mMedications.get(0).equals("i.e. Tylenol")){
                mMedications.set(0,medication);
            } else {
                addMedication(medication);
            }
        }
        new_medication.setText("");
        updateUI();
    }

    /**
     * Remove cursor from alert name EditText when not selected
     */
    View.OnClickListener alertNameClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == alert_name_et.getId()) {
                alert_name_et.setCursorVisible(true);
            }
        }
    };

    /**
     * Show cursor on new_medication EditText when it is highlighted
     */
    View.OnClickListener newMedicationClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == new_medication.getId()) {
                new_medication.setCursorVisible(true);
            }
        }
    };

    // saves the new med alert
    public void save (MenuItem v) {

        // save all information to and update account
        if (!checkFields()) {
            return;
        }

        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_OK, resultIntent);

        // need to save current information
        resultIntent.putExtra(ALERT_NAME, mAlertName);
        resultIntent.putExtra(OLD_ALERT_NAME, oldAlertName);

        if (mAlertTime != null) {
            resultIntent.putExtra(ALERT_TIME, mAlertTime.getTime());
        }
        //resultIntent.putExtra(ALERT_RECURRENCE_TYPE, mRecurrenceType);
        resultIntent.putExtra(ALERT_DAYS_OF_WEEK, mDaysOfWeek);
        resultIntent.putExtra(ALERT_MEDS_TAKEN, mMedsTaken);
        resultIntent.putStringArrayListExtra(ALERT_MEDICATION, mMedications);

        Toast.makeText(this, "Saved Medication Alert", Toast.LENGTH_SHORT).show();
        finish();
    }

    public boolean checkFields() {
        // get alert name
        mAlertName = alert_name_et.getText().toString();
        if (mAlertName == null || mAlertName.equals("")) {
            Toast.makeText(this,"Please Enter Alert Name", Toast.LENGTH_SHORT).show();
            return false;
        } else if (mAlertTime == null) {
            Toast.makeText(this,"Please Enter Alert Time", Toast.LENGTH_SHORT).show();
            return false;
        } else if (checkRecurrences() == 0) {
            Toast.makeText(this,"Please Set Alert For At Least One Day In Week", Toast.LENGTH_SHORT).show();
            return false;
        } else if (mMedications.get(0).equals("i.e. Tylenol")) {
            Toast.makeText(this,"Please Add A Medication", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    // updates UI with most recent data
    public void updateUI() {

        TextView alert_time = (TextView) findViewById(R.id.alert_time);

        String standardTime = "";
        try {
            if (mAlertTime != null) {
                final SimpleDateFormat sdf = new SimpleDateFormat("H:mm");
                final Date dateObj = sdf.parse(mAlertTime.toString());
                standardTime = new SimpleDateFormat("hh:mm a").format(dateObj);
            }
        } catch (final ParseException e) {
            e.printStackTrace();
        }

        if (mAlertTime != null) {
            alert_time.setText(standardTime);
        }

        Spinner recurrence = (Spinner) findViewById(R.id.recurrence_spinner);
        recurrence.setSelection(mRecurrenceType);

        ListView medications = (ListView) findViewById(R.id.medication_list);
        ArrayAdapter list_adapter = (ArrayAdapter) medications.getAdapter();
        list_adapter.notifyDataSetChanged();
    }

    // locks and unlocks checkbox options based on spinner input
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

    // creating adapter for medication listview
    public void setMedicationAdapter() {
        // set array adapter for listview of medication
        final ListView medications = (ListView) findViewById(R.id.medication_list);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mMedications){

            @Override
            public View getView(int position, View convertView,
                                ViewGroup parent) {
                View view =super.getView(position, convertView, parent);
                TextView textView=(TextView) view.findViewById(android.R.id.text1);

                if (mMedications.get(0).equals("i.e. Tylenol"))
                    textView.setTextColor(Color.LTGRAY);
                else
                    textView.setTextColor(Color.DKGRAY);
                return view;
            }
        };
        medications.setAdapter(adapter);

        medications.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView tv = (TextView) view;
                displayDeleteMedicationDialog(tv.getText().toString(), position);
            }
        });
    }

    // option to delete medication in alert
    public void displayDeleteMedicationDialog(String name, final int index) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Delete Medication: " + name + "?");
        builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                mMedications.remove(index);
                checkMedList();

                // update listview with new information
                updateUI();
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

    // find which days were checked off
    int checkRecurrences() {

        int checked_days = 0;

        if (isChecked(R.id.chkbx_monday)) {
            mDaysOfWeek[1] = 1;
            checked_days++;
        } else
            mDaysOfWeek[1] = 0;

        if (isChecked(R.id.chkbx_tuesday)){
            mDaysOfWeek[2] = 1;
            checked_days++;
        } else
            mDaysOfWeek[2] = 0;

        if (isChecked(R.id.chkbx_wednesday)){
            mDaysOfWeek[3] = 1;
            checked_days++;
        } else
            mDaysOfWeek[3] = 0;

        if (isChecked(R.id.chkbx_thursday)){
            mDaysOfWeek[4] = 1;
            checked_days++;
        } else
            mDaysOfWeek[4] = 0;

        if (isChecked(R.id.chkbx_friday)){
            mDaysOfWeek[5] = 1;
            checked_days++;
        } else {
            mDaysOfWeek[5] = 0;
        }

        if (isChecked(R.id.chkbx_saturday)){
            mDaysOfWeek[6] = 1;
            checked_days++;
        } else {
            mDaysOfWeek[6] = 0;
        }

        if (isChecked(R.id.chkbx_sunday)){
            mDaysOfWeek[0] = 1;
            checked_days++;
        } else {
            mDaysOfWeek[0] = 0;
        }

        return checked_days;
    }

    // checks to see if checkbox is checked
    public boolean isChecked(int id) {
        CheckBox box = (CheckBox) findViewById(id);
        return box.isChecked();
    }

    // endable all days to be checkable
    public void enableAllDays() {
        enableCheckbox(R.id.chkbx_monday);
        enableCheckbox(R.id.chkbx_tuesday);
        enableCheckbox(R.id.chkbx_wednesday);
        enableCheckbox(R.id.chkbx_thursday);
        enableCheckbox(R.id.chkbx_friday);
        enableCheckbox(R.id.chkbx_saturday);
        enableCheckbox(R.id.chkbx_sunday);
    }

    // disable all days from being checkable
    public void disableAllDays() {
        disableCheckbox(R.id.chkbx_monday);
        disableCheckbox(R.id.chkbx_tuesday);
        disableCheckbox(R.id.chkbx_wednesday);
        disableCheckbox(R.id.chkbx_thursday);
        disableCheckbox(R.id.chkbx_friday);
        disableCheckbox(R.id.chkbx_saturday);
        disableCheckbox(R.id.chkbx_sunday);
    }

    // enable checkbox
    public void enableCheckbox(int id) {
        CheckBox box = (CheckBox) findViewById(id);
        if (box != null) {
            box.setChecked(false);
            box.setEnabled(true);
        }
    }

    // disable check box
    public void disableCheckbox(int id) {
        CheckBox box = (CheckBox) findViewById(id);
        if (box != null) {
            box.setChecked(true);
            box.setEnabled(false);
        }
    }

    // check that medlist isn't empty
    private void checkMedList() {
        if (mMedications.size() == 0){
            mMedications.add(0,"i.e. Tylenol");
        }
    }

    public void addMedication(String medication) {
        mMedications.add(medication);
    }

    public void setupUI(View view) {
        //Set up touch listener for non-text box views to hide keyboard.
        if(!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard();
                    return false;
                }
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }

    public void hideSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager)  getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }

}
