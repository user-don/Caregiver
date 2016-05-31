package edu.cs65.caregiver.caregiver;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.gson.Gson;

import java.io.IOException;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.cs65.caregiver.backend.messaging.Messaging;
import edu.cs65.caregiver.backend.messaging.model.CaregiverEndpointsObject;
import edu.cs65.caregiver.backend.registration.Registration;
import edu.cs65.caregiver.caregiver.model.CareGiver;
import edu.cs65.caregiver.caregiver.model.MedicationAlert;
import edu.cs65.caregiver.caregiver.model.Recipient;
import edu.cs65.caregiver.caregiver.model.RecipientToCareGiverMessage;


public class CareRecipientActivity extends Activity implements ServiceConnection {

    private static final String TAG = "CareRecipientActivity";

    private CareGiver mCareGiver;
    private Recipient mReceiver;
    private long mCheckInTime;
    private int mDay;
    private int dayIndex;

    private List<String> listValues;
    private ArrayList<MedicationAlert> mMedicationAlerts;

    /* --- medication stuff --- */
    public static ArrayList<MedEntry> sortedMeds;
    public static MediaPlayer mediaPlayer;
    public static Vibrator v;
    public static boolean loadMedDialog = false;

    /* --- cloud stuff --- */
    public static String SERVER_ADDR = "https://handy-empire-131521.appspot.com";
    private static final String SENDER_ID = "1059275309009";
    CareGiver cloudData;
    private boolean mReceiverRegistered = false;
    public static String mRegistrationID;

    /* --- service stuff --- */
    SensorService mySensorService;
    ReceiveMessages myReceiver = null;
    boolean myReceiverIsRegistered = false;
    boolean mIsBound;
    private ServiceConnection mConnection = this;
    public static Context mContext;

    private CareRecipientBroadcastReceiver mBroadcastReceiver;
    private IntentFilter mIntentFilter;

    public static String mEmail = "dummy";
    public static String mRecipientName = "test";
    private static final String EMAIL_KEY = "email key";
    private static final String REGISTRATION_KEY = "registration key";
    private static final String RECIPIENT_NAME_KEY = "recipient name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_care_recipient);
        mContext = getApplicationContext();

        // get email and registrationID
        SharedPreferences preferences = getSharedPreferences(getString(R.string.profile_preference), 0);
        mEmail = preferences.getString(EMAIL_KEY, "");
        mRecipientName = preferences.getString(RECIPIENT_NAME_KEY, "");
        mRegistrationID = preferences.getString(REGISTRATION_KEY, "");

        // register receiver for gcm message broadcasts
        mBroadcastReceiver = new CareRecipientBroadcastReceiver();
        mIntentFilter = new IntentFilter("edu.cs65.caregiver.caregiver.CARERECIPIENT_BROADCAST");
        registerReceiver(mBroadcastReceiver, mIntentFilter);

        // connect service
        myReceiver = new ReceiveMessages();
        mIsBound = false;
        automaticBind();

        // store current day of week
        getDayOfWeek();

        // show recipient name in toolbar
        Toolbar header = (Toolbar) findViewById(R.id.toolbar);
        header.setTitle(mRecipientName);

        // get CareGiver info
        GetCareGiverInfoAsyncTask task = new GetCareGiverInfoAsyncTask();
        task.email = mEmail;
        task.execute();
    }


    public void getDayOfWeek() {
        Calendar calendar = Calendar.getInstance();
        mDay = calendar.get(Calendar.DAY_OF_WEEK);
        System.out.println("current day of week: " + mDay);
        dayIndex = mDay - 1; // convert to match MedicationAlert int values
        System.out.println("dayIndex: " + dayIndex);
    }

    /* ––––– displays a custom dialog for the current medication to take ––––– */
    public void displayMedDialog(final MedEntry entry) {
        String meds[] = new String[entry.meds.size()];
        meds = entry.meds.toArray(meds);

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        SpannableString spanString = new SpannableString(" " + entry.label);
        spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, spanString.length(), 0);

        TextView title = new TextView(getApplicationContext());
        title.setText(spanString);
        title.setTextSize(32);
        title.setTextColor(getColor(R.color.darkgrey));
        title.setGravity(Gravity.BOTTOM);

        alertBuilder.setCustomTitle(title);

        LayoutInflater inflater = getLayoutInflater();
        View convertView = (View) inflater.inflate(R.layout.custom, null);
        alertBuilder.setView(convertView);
        ListView lv = (ListView) convertView.findViewById(R.id.listView1);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.med_list,meds);
        lv.setAdapter(adapter);

        alertBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                if (loadMedDialog) {
                    stopAlarm();
                    loadMedDialog = false;

                    // notify CareGiver that medicine was taken
                    new AsyncTask<Void, Void, Void>() {
                        private static final String TAG = "Notify meds taken AT";

                        @Override
                        protected Void doInBackground(Void... params) {

                            Messaging.Builder builder =
                                    new Messaging
                                            .Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null)
                                            .setRootUrl(SERVER_ADDR + "/_ah/api/");
                            Messaging backend = builder.build();
                            Log.d(TAG, "Notifying caregiver of meds taken: " + mEmail);
                            try {

                                // make help message
                                RecipientToCareGiverMessage msg =
                                        new RecipientToCareGiverMessage(RecipientToCareGiverMessage.MED_TAKEN,
                                                entry.alerts,
                                                Calendar.getInstance().getTime().getTime());

                                backend.sendNotificationToCaregiver(mRegistrationID, mEmail, msg.selfToString()).execute();
                            } catch (IOException e) {
                                Log.d(TAG, "send med taken failed");
                                e.printStackTrace();
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void result) {
                            Log.d(TAG, "sent med taken message\n");
                        }
                    }.execute();
                }

            }
        }).setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (loadMedDialog) {
                    stopAlarm();
                    loadMedDialog = false;

                    // notify CareGiver that medicine was not taken
                    new AsyncTask<Void, Void, Void>() {
                        private static final String TAG = "Notify meds taken AT";

                        @Override
                        protected Void doInBackground(Void... params) {

                            Messaging.Builder builder =
                                    new Messaging
                                            .Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null)
                                            .setRootUrl(SERVER_ADDR + "/_ah/api/");
                            Messaging backend = builder.build();
                            Log.d(TAG, "Notifying caregiver meds not taken: " + mEmail);
                            try {

                                // make help message
                                RecipientToCareGiverMessage msg =
                                        new RecipientToCareGiverMessage(RecipientToCareGiverMessage.MED_NOT_TAKEN,
                                                entry.alerts,
                                                Calendar.getInstance().getTime().getTime());

                                backend.sendNotificationToCaregiver(mRegistrationID, mEmail, msg.selfToString()).execute();
                            } catch (IOException e) {
                                Log.d(TAG, "send med not taken failed");
                                e.printStackTrace();
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void result) {
                            Log.d(TAG, "sent med not taken message\n");
                        }
                    }.execute();

                }
            }
        });

        final AlertDialog alertDialog = alertBuilder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button btnPositive = alertDialog.getButton(Dialog.BUTTON_POSITIVE);
                btnPositive.setTextSize(25);

            }
        });

        alertDialog.create();
        alertDialog.show();

    }

    /* ––––– HELP button handler ––––– */
    // when help is pressed, send HELP message to the associated CareGiver
    public void onHelpClicked(View v) {
        final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);

        SpannableString spanString = new SpannableString("  Do you need help?" + "\n");
        spanString.setSpan(new StyleSpan(Typeface.NORMAL), 0, spanString.length(), 0);

        TextView title = new TextView(getApplicationContext());
        title.setText(spanString);
        title.setTextSize(32);
        title.setTextColor(getColor(R.color.darkgrey));
        title.setGravity(Gravity.LEFT | Gravity.BOTTOM);

        alertBuilder.setCustomTitle(title);

        alertBuilder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                new AsyncTask<Void, Void, Void>() {
                    private static final String TAG = "Notify HELP AT";

                    @Override
                    protected Void doInBackground(Void... params) {

                        Messaging.Builder builder =
                                new Messaging
                                        .Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null)
                                        .setRootUrl(SERVER_ADDR + "/_ah/api/");

                        Messaging backend = builder.build();


                        Log.d(TAG, "Notifying caregiver of help: " + mEmail);
                        try {

                            // make help message
                            RecipientToCareGiverMessage msg =
                                    new RecipientToCareGiverMessage(RecipientToCareGiverMessage.HELP,
                                            null,
                                            Calendar.getInstance().getTime().getTime());

                            backend.sendNotificationToCaregiver(mRegistrationID, mEmail, msg.selfToString()).execute();
                        } catch (IOException e) {
                            Log.d(TAG, "send help failed");
                            e.printStackTrace();
                        }

                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void result) {
                        Log.d(TAG, "sent help message\n");
                        Toast.makeText(getApplicationContext(), "CAREGIVER HAS BEEN ALERTED",
                                Toast.LENGTH_LONG).show();
                    }
                }.execute();

            }
        }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        // custom dialog box with bigger font for elderly users
        final AlertDialog alertDialog = alertBuilder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button btnPositive = alertDialog.getButton(Dialog.BUTTON_POSITIVE);
                btnPositive.setTextSize(25);
                btnPositive.setGravity(Gravity.CENTER);

                Button btnNegative = alertDialog.getButton(Dialog.BUTTON_NEGATIVE);
                btnNegative.setTextSize(25);
                btnNegative.setGravity(Gravity.CENTER);
            }
        });
        alertDialog.create();
        alertDialog.show();

    }

    /* ––––– MEDICATION DETAILS button handler ––––– */
    // when clicked, loads custom expandable list view for today's scheduled meds
    public void onMedClicked(View v) {
        // update sortedMeds
        if (sortedMeds.size() != 0) {
            DialogFragment fragment = CareGiverDialogFragment.newInstance(CareGiverDialogFragment.DISPLAY_MED_LIST);
            fragment.show(getFragmentManager(),
                    getString(R.string.app_name));
        } else {
            Toast.makeText(getApplicationContext(), "No medications scheduled for today", Toast.LENGTH_LONG).show();
        }
    }

    public void onMenuClicked(View v) {
    }

    /* ––––– helper function to load medications by time for current day ––––– */
    public void setUpAdapter() {
        ArrayList<MedicationAlert> todaysMeds = getMedsForToday();
        sortedMeds = getGroupedMeds(todaysMeds);
        listValues = new ArrayList<String>();

        for (int i = 0; i < sortedMeds.size(); i++) {
            String title = convertTime(sortedMeds.get(i).time);
            sortedMeds.get(i).label = title;
            listValues.add(title + " Medications");
        }

    }

    /* ––––– sorts medications, returning only those for current day ––––– */
    public ArrayList<MedicationAlert> getMedsForToday() {
        ArrayList<MedicationAlert> medsForToday = new ArrayList<>();

        if (mMedicationAlerts != null) {
            for (int i = 0; i < mMedicationAlerts.size(); i++) {
                int[] alertDays = mMedicationAlerts.get(i).mAlertDays;
                if (alertDays[dayIndex] != 0) {
                    medsForToday.add(mMedicationAlerts.get(i));
                }
            }
        }
        return medsForToday;
    }

    /* ––––– groups medications by time of day ––––– */
    public ArrayList<MedEntry> getGroupedMeds(ArrayList<MedicationAlert> todaysMeds) {
        ArrayList<MedEntry> sortedMeds = new ArrayList<>();

        // if only one medication is scheduled, create MedEntry and return
        if (todaysMeds.size() == 1) {
            MedEntry newEntry = new MedEntry(todaysMeds.get(0).mTime.toString(),
                    todaysMeds.get(0).mMedications, todaysMeds.get(0).mTime,
                    todaysMeds.get(0).mName);
            sortedMeds.add(newEntry);
            return sortedMeds;
        }

        // Otherwise, compare times of todaysMeds and group by time
        int countOfSortedMeds = 0;
        for (int i = 0; i < todaysMeds.size() - 1; i++) {
            int j = i + 1;

            if (i == 0) { // automatically add first medication
                MedEntry newEntry = new MedEntry(todaysMeds.get(i).mTime.toString(),
                        todaysMeds.get(i).mMedications, todaysMeds.get(i).mTime,
                        todaysMeds.get(i).mName);
                sortedMeds.add(newEntry);
                countOfSortedMeds++;
            }
            // compare medication alert times
            if (todaysMeds.get(i).compareTo(todaysMeds.get(j)) != 0) {
                MedEntry entry = new MedEntry(todaysMeds.get(j).mTime.toString(),
                        todaysMeds.get(j).mMedications, todaysMeds.get(j).mTime,
                        todaysMeds.get(j).mName);
                sortedMeds.add(entry);
                countOfSortedMeds++;
            }
            // if found duplicate time, append medications to existing MedEntry object
            else {
                String name = todaysMeds.get(j).mTime.toString();
                Time time = todaysMeds.get(j).mTime;

                // add meds to current MedEntry
                for (int k = 0; k < todaysMeds.get(j).mMedications.size(); k++) {
                    sortedMeds.get(countOfSortedMeds - 1).addMedToEntry(todaysMeds.get(j).mMedications.get(k));
                }

                // add medication group name
                sortedMeds.get(countOfSortedMeds - 1).addEntryName(todaysMeds.get(j).mName);
            }
        }

        return sortedMeds;
    }

    public static String convertTime(Time time) {
        String rawTime = time.toString();
        String convertedTime = null;

        try {
            final SimpleDateFormat _24HourSDF = new SimpleDateFormat("HH:mm");
            final SimpleDateFormat _12HourSDF = new SimpleDateFormat("hh:mm a");
            final Date _24HrDt = _24HourSDF.parse(rawTime);
            convertedTime = _12HourSDF.format(_24HrDt);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return convertedTime; // return time in String 12-hour AM/PM format
    }

    private void startAlarm() {
        // start vibration and alarm sound
        mediaPlayer = MediaPlayer.create(this,
                RingtoneManager.getDefaultUri((RingtoneManager.TYPE_ALARM)));
        mediaPlayer.start();

        v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {0, 500, 1000};
        v.vibrate(pattern, 0);
    }

    public static void stopAlarm() {
        // stop vibration and alarm sound
        mediaPlayer.stop();
        v.cancel();
    }

    public void loadData() {
        // load data from cloud and save locally
        mReceiver = cloudData.getRecipient(mRecipientName);
        if (mReceiver != null) {
            mMedicationAlerts = mReceiver.mAlerts;
            mCheckInTime = mReceiver.mCheckIntime;
        }
    }

    public void updateUI() {
        loadData();
        setUpAdapter();
        PSMScheduler.setSchedule(CareRecipientActivity.this); // update alarms
    }

    /* ––––– sets new check-in time ––––– */
    public void updateCheckInTime() {
        if ((Long) mCheckInTime != null) {
            PSMScheduler.setCheckinAlarm(this, mCheckInTime);
        }
    }

    // –––––––––––––––––––––––––– BroadcastReceiver –––––––––––––––––––––––––– //

    public class CareRecipientBroadcastReceiver extends BroadcastReceiver {
        private static final String TAG = "CareRecipientReceiver";

        @Override
        public void onReceive(Context c, Intent i) {

            if (i.getBooleanExtra("take meds", false) == true) {
                System.out.println("CareRecipientActivity loadMedDialog=TRUE");

                startAlarm();
                int index = i.getExtras().getInt("index");
                MedEntry entry = sortedMeds.get(index);
                displayMedDialog(entry);
            } else {
                Log.d(TAG, "Received broadcast -> updating information");
                new GetCareGiverInfoAsyncTask().execute();
            }


        }
    }

    // –––––––––––––––––––––––––– life cycle methods –––––––––––––––––––––––––– //

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "C:onDestroy()");

        unregisterReceiver(mBroadcastReceiver);
        try {
            doUnbindService();
        } catch (Throwable t) {
            Log.e(TAG, "Failed to unbind from the service", t);
        }
    }


    // –––––––––––––––––––––––––– service methods –––––––––––––––––––––––––– //

    private void automaticBind() {
        doBindService();
    }

    private void doBindService() {
        bindService(new Intent(this, SensorService.class), mConnection,
                Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    private void doUnbindService() {
        Log.d(TAG, "C:doUnBindService()");
        if (mIsBound) {
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        SensorService.MyLocalBinder binder = (SensorService.MyLocalBinder) service;
        mySensorService = binder.getService();
        mIsBound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mIsBound = false;
    }

    // –––––––––––––––––––––––––– receiver methods –––––––––––––––––––––––––– //

    public class ReceiveMessages extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(SensorService.BROADCAST_LABEL_CHANGE)) {
                // notify CareGiver that help is needed
            }

        }
    }


    // –––––––––––––––––––––––––– inner classes –––––––––––––––––––––––––– //

    /* –– class for displaying grouped medications –– */
    //      instance variables:
    //      1. String label: time of medications to be displayed in UI
    //      2. Arraylist<String> meds: list of medications associated with time
    //      3. Time time: time of medications
    //      4. Arraylist<String> alerts: list of MedicationAlert.mNames associated with each med
    public class MedEntry {
        public String label;
        public ArrayList<String> meds;
        public final Time time;
        public ArrayList<String> alerts = new ArrayList<>();

        // constructor
        public MedEntry(String name, ArrayList<String> meds, Time time, String alertName) {
            this.label = name;
            this.meds = meds;
            this.time = time;
            this.alerts.add(alertName);
        }

        // method to add medication to MedEntry.meds
        public void addMedToEntry(String newMed) {
            this.meds.add(newMed);
        }

        // method to add medication name to MedEntry.alerts
        public void addEntryName(String alertName) {
            this.alerts.add(alertName);
        }

    }

    /* –– AsyncTask for getting CareGiver information –– */
    class GetCareGiverInfoAsyncTask extends AsyncTask<Void, String, String> {

        private static final String TAG = "Get Account Info AT";
        public String email = "";
        Gson gson;

        @Override
        protected String doInBackground(Void... params) {
            Messaging.Builder builder =
                    new Messaging
                            .Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null)
                            .setRootUrl(SERVER_ADDR + "/_ah/api/");

            Messaging backend = builder.build();
            CaregiverEndpointsObject response = null;

            Log.d(TAG, "Executing getAccountInfo with email " + mEmail);
            try {
                response = backend.getAccountInfo(mEmail).execute();
            } catch (IOException e) {
                Log.d(TAG, "getAccountInfo failed");
                e.printStackTrace();
            }

            Log.d(TAG, "Cannot update account because device is unregistered");


            return (response != null) ? response.getData() : null;
        }

        @Override
        protected void onPostExecute(String data) {
            gson = new Gson();
            if (data != null) {
                Log.d(TAG, "Updating CareGiver Information");
                Log.d(TAG, "got data: " + data);
                cloudData = gson.fromJson(data, CareGiver.class);
                updateUI();
                updateCheckInTime();

            }
        }
    }


}
