package edu.cs65.caregiver.caregiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.gcm.GcmListenerService;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.gson.Gson;

import java.io.IOException;
import java.sql.Time;
import java.util.ArrayList;

import edu.cs65.caregiver.caregiver.controllers.DataController;
import edu.cs65.caregiver.caregiver.model.CareGiver;
import edu.cs65.caregiver.caregiver.model.MedicationAlert;
import edu.cs65.caregiver.caregiver.model.Recipient;
import edu.cs65.caregiver.backend.messaging.Messaging;
import edu.cs65.caregiver.caregiver.model.RecipientToCareGiverMessage;

public class CareGiverActivity extends AppCompatActivity {

    private static final String TAG = "CareGiverActivity";
    public static String SERVER_ADDR = "https://handy-empire-131521.appspot.com";

    // Changed sender id
    private static final String SENDER_ID = "1059275309009";

    public static final int NEW_MEDICATION_REQUEST = 1;
    public static final int EDIT_MEDICATION_REQUEST = 2;
    public static final String ADDED_MEDICATION_ALERT = "new alert";

    DataController mDataController;

    //private CareGiver mCareGiver;
    private String mEmail = "dummy";
    private String mRecipientName = "test";
    private Recipient mReceiver;

    private static final String EMAIL_KEY = "email key";
    private static final String REGISTRATION_KEY = "registration key";
    private static final String RECIPIENT_NAME_KEY = "recipient name";
    private SharedPreferences mPrefs;

    /* --- cloud stuff --- */
    private boolean mReceiverRegistered = false;
    private String mRegistrationID;
    private CareGiverBroadcastReceiver mBroadcastReceiver;
    private IntentFilter mIntentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_care_giver);

        mPrefs = getSharedPreferences(getString(R.string.profile_preference), 0);
        mEmail = mPrefs.getString(EMAIL_KEY,"");
        mRecipientName = mPrefs.getString(RECIPIENT_NAME_KEY,"");
        mRegistrationID = mPrefs.getString(REGISTRATION_KEY,"");

        mBroadcastReceiver = new CareGiverBroadcastReceiver();
        mIntentFilter = new IntentFilter("edu.cs65.caregiver.caregiver.CAREGIVER_BROADCAST");

        mDataController = DataController.getInstance(getApplicationContext());
        mDataController.initializeData(getApplicationContext());
        mDataController.loadData();

//        mReceiver = mDataController.careGiver.getRecipient(mRecipientName);
//        if (mDataController.careGiver.getRecipient(mRecipientName) == null) {
//            mDataController.careGiver.addRecipient(mRecipientName);
//            mDataController.saveData();
//        }

        mReceiver = mDataController.careGiver.getRecipient(mRecipientName);
        if (mReceiver == null) {
            Log.d(TAG, "Initializing recipient " + mRecipientName);
            mReceiver = mDataController.careGiver.addRecipient(mRecipientName);
            mDataController.saveData();
        }

        setAlertAdapter();
        updateUI();

        GetCareGiverInfoAsyncTask task = new GetCareGiverInfoAsyncTask();
        task.email = mEmail;
        task.execute();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.care_giver_activity_menu, menu);

        Toolbar toolbarBottom = (Toolbar) findViewById(R.id.toolbar_bottom);
        toolbarBottom.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return true;
            }
        });

        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mDataController.saveData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mBroadcastReceiver, mIntentFilter);
        //registerReceiver();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(mBroadcastReceiver);
        //LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        //isReceiverRegistered = false;
        super.onPause();
    }

    public void onClickNewMedication(MenuItem menuItem) {
        Intent i = new Intent(this, NewMedicationActivity.class);
        startActivityForResult(i, NEW_MEDICATION_REQUEST);
    }

//    public void onClickAccount(MenuItem menuItem) {
//        // TODO -- should have some account management activity
//
//        Log.d(TAG, "test ");
//        new AsyncTask<Void,Void,Void>() {
//            @Override
//            protected Void doInBackground(Void... params) {
//                Gson gson = new Gson();
//
//                edu.cs65.caregiver.backend.messaging.Messaging.Builder builder =
//                        new edu.cs65.caregiver.backend.messaging.Messaging
//                                .Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null)
//                                .setRootUrl(SERVER_ADDR + "/_ah/api/");
//
//                edu.cs65.caregiver.backend.messaging.Messaging backend = builder.build();
//                try {
//                    Log.d(TAG, "send \'testing\' to test func");
//                    backend.helloTest("testing").execute();
//                } catch (IOException e) {
//                    Log.d(TAG, "failed to issue post - Error msg: " + e.getMessage());
//                    e.printStackTrace();
//                }
//                return null;
//            }
//
//        }.execute();
//
//
//        // dummy information below
////        Log.d(TAG, "executing account post");
////        new AsyncTask<Void,Void,Void>() {
////            @Override
////            protected Void doInBackground(Void... params) {
////                Gson gson = new Gson();
////
////                HashMap<String, String> account_params = new HashMap<>();
////                account_params.put("email", mEmail);
////                account_params.put("password","dummy_pass");
////                account_params.put("registrationId", mRegistrationID);
////                account_params.put("caregiver", gson.toJson(mDataController.careGiver));
////
////                try {
////                    String response = ServerUtilities.post(SERVER_ADDR + "/create_account.do", account_params);
////                    Log.d(TAG, "post response: " + response);
////                } catch (IOException e) {
////                    Log.d(TAG, "failed to issue post - Error msg: " + e.getMessage());
////                    e.printStackTrace();
////                }
////                return null;
////            }
////
////        }.execute();
//
//
//    }


    public void onClickCheckInStatus(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if (mReceiver.mHasCheckedInToday) {
            String time_str = new Time(mReceiver.mCheckedInTime).toString();
            builder.setTitle(mReceiver.mName + " checked in at " + time_str);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {

                }
            });
        } else {

            builder.setTitle(mReceiver.mName + " has not checked in today");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {

                }
            });
        }
        builder.create().show();
    }

    public void onClickAlertStatus(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if (mReceiver.mRaisedAlert) {
            String time_str = new Time(mReceiver.mCheckedInTime).toString();
            builder.setTitle(mReceiver.mName + " needs assistance!");
            builder.setPositiveButton("CLEAR", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    mReceiver.mRaisedAlert = false;
                    updateUI();
                }
            });
            builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {

                }
            });
        } else {
            builder.setTitle(mReceiver.mName + " hasn't raised an ALERT");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {

                }
            });

        }
        builder.create().show();
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

                    Log.d(TAG, "adding alert name " + newAlert.mName);
                    mReceiver.addAlert(newAlert);
                    mReceiver = mDataController.setRecipientData(mReceiver);
                    //mDataController.careGiver.setAlert(mRecipientName,newAlert);
                    //mDataController.careGiver.getRecipient(mRecipientName).addAlert(newAlert);
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
                    mDataController.setRecipientData(mReceiver);
                }
                break;
        }

        mDataController.saveData();
        new UpdateCareGiverAsyncTask().execute();

        SendMessageToPatientAsyncTask task = new SendMessageToPatientAsyncTask();
        task.msg = "UPDATE";
        task.execute();

        updateUI();
    }

    public void updateUI() {

        mDataController.loadData();
        //mReceiver = mDataController.careGiver.getRecipient(mRecipientName);

        ListView alertList = (ListView) findViewById(R.id.medication_alert_list2);
        ((ArrayAdapter) alertList.getAdapter()).notifyDataSetChanged();

        TextView recipientText = (TextView) findViewById(R.id.caregiver_recipient);
        recipientText.setText(" " + mRecipientName);

//        Button mAlertButton = (Button) findViewById(R.id.alert_status_button);
//        if (mReceiver.mRaisedAlert) {
//            mAlertButton.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
//            //mAlertButton.setBackgroundColor(Color.RED);
//        } else {
//            mAlertButton.getBackground().setColorFilter(Color.LTGRAY, PorterDuff.Mode.MULTIPLY);
//            //mAlertButton.setBackgroundColor(Color.GRAY);
//        }
//
//        Button mStatusButton = (Button) findViewById(R.id.checkin_status_button);
//        if (mReceiver.mHasCheckedInToday) {
//            mStatusButton.getBackground().setColorFilter(Color.BLUE, PorterDuff.Mode.MULTIPLY);
//            //mStatusButton.setcolor(Color.BLUE);
//        } else {
//            mStatusButton.getBackground().setColorFilter(Color.LTGRAY, PorterDuff.Mode.MULTIPLY);
//            //mStatusButton.setBackgroundColor(Color.GRAY);
//        }
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

                mDataController.setRecipientData(mReceiver);
                mDataController.saveData();
                new UpdateCareGiverAsyncTask().execute();
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

    public class CareGiverBroadcastReceiver extends BroadcastReceiver {
        private static final String TAG = "CareGiverNotification";

        @Override
        public void onReceive(Context c, Intent i) {
            Log.d(TAG, "received notification broadcast");

            Gson gson = new Gson();
            RecipientToCareGiverMessage msg = gson.fromJson(i.getStringExtra("msg"),RecipientToCareGiverMessage.class);
            switch(msg.messageType) {
                case RecipientToCareGiverMessage.CHECKIN:
                    mReceiver.mCheckedIn = true;
                    mDataController.setRecipientData(mReceiver);
                    mDataController.saveData();

                    new UpdateCareGiverAsyncTask().execute();

                    updateUI();
                    break;

                case RecipientToCareGiverMessage.MED_TAKEN:

                    for (String alert : msg.medAlertNames) {
                        Log.d(TAG, "Recipient has taken " + alert);
                        // checkOffAlert()
                        // TODO need to check off alerts that have been taken
                    }

                    break;

                case RecipientToCareGiverMessage.MED_NOT_TAKEN:

                    break;

                case RecipientToCareGiverMessage.HELP:
                    mReceiver.mRaisedAlert = true;
                    mDataController.setRecipientData(mReceiver);
                    mDataController.saveData();

                    updateUI();
                    break;
            }
        }
    }

//    // GCM registration ... called in Main Activity
//    class GcmRegistrationAsyncTask extends AsyncTask<Void, Void, String> {
//        private Registration regService = null;
//        private GoogleCloudMessaging gcm;
//        private Context context;
//
//        public GcmRegistrationAsyncTask(Context context) {
//            this.context = context;
//        }
//
//        @Override
//        protected String doInBackground(Void... params) {
//            if (regService == null) {
//                Registration.Builder builder = new Registration.Builder(AndroidHttp.newCompatibleTransport(),
//                        new AndroidJsonFactory(), null)
//                        .setRootUrl(SERVER_ADDR + "/_ah/api/");
//                // UNCOMMENT TO RUN LOCALLY
////                        .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
////                            @Override
////                            public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest)
////                                    throws IOException {
////                                abstractGoogleClientRequest.setDisableGZipContent(true);
////                            }
////                        });
//                // end of optional local run code
//
//                regService = builder.build();
//            }
//
//            String msg = "";
//            try {
//                if (gcm == null) {
//                    gcm = GoogleCloudMessaging.getInstance(context);
//                }
//                mRegistrationID = gcm.register(SENDER_ID);
//                msg = "Device registered, registration ID = " + mRegistrationID;
//
//                // Send registration ID to server over HTTP so it can use GCM/HTTP
//                // to send messages to the app.
//                regService.register(mRegistrationID).execute();
//
//            } catch (IOException ex) {
//                ex.printStackTrace();
//                Log.d(TAG, "Error: " + ex.getMessage());
//                msg = null;
//            }
//            return msg;
//        }
//
//        @Override
//        protected void onPostExecute(String msg) {
//
//            //Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
//            if (msg != null) {
//                Logger.getLogger("REGISTRATION").log(Level.INFO, msg);
//                Toast.makeText(context, "Connected to Cloud!", Toast.LENGTH_SHORT).show();
//                mReceiverRegistered = true;
//
//                // update info
//                GetCareGiverInfoAsyncTask task = new GetCareGiverInfoAsyncTask();
//                task.email = mEmail;
//                task.execute();
//
//            } else {
//                Toast.makeText(context, "Failed to Connect to Cloud", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }

    class GetCareGiverInfoAsyncTask extends AsyncTask<Void,String,String> {
        private static final String TAG = "Get Account Info AT";
        public String email = "";
        Gson gson;

        @Override
        protected String doInBackground(Void... params) {

            edu.cs65.caregiver.backend.messaging.Messaging.Builder builder =
                    new edu.cs65.caregiver.backend.messaging.Messaging
                            .Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null)
                            .setRootUrl(SERVER_ADDR + "/_ah/api/");

            edu.cs65.caregiver.backend.messaging.Messaging backend = builder.build();
            edu.cs65.caregiver.backend.messaging.model.CaregiverEndpointsObject response = null;

            Log.d(TAG, "Executing getAccountInfo with email " + email);
            try {
                response = backend.getAccountInfo(email).execute();
            } catch (IOException e) {
                Log.d(TAG, "getAccountInfo failed");
                e.printStackTrace();
            }

            return (response != null) ? response.getData() : null;
        }

        protected void onPostExecute(String data) {
            gson = new Gson();
            if (data != null) {
                Log.d(TAG,"Refreshing local CareGiver Information: " + data);
                CareGiver cloudData = gson.fromJson(data, CareGiver.class);
                mDataController.setData(cloudData);
                mDataController.saveData();
                updateUI();
            }
        }
    }

    class UpdateCareGiverAsyncTask extends AsyncTask<Void,Void,Void> {
        private static final String TAG = "Update Account Info AT";

        @Override
        protected Void doInBackground(Void... params) {

            edu.cs65.caregiver.backend.messaging.Messaging.Builder builder =
                    new edu.cs65.caregiver.backend.messaging.Messaging
                            .Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null)
                            .setRootUrl(SERVER_ADDR + "/_ah/api/");

            edu.cs65.caregiver.backend.messaging.Messaging backend = builder.build();

            if (mReceiverRegistered) {
                Log.d(TAG, "Executing update account with email " + mEmail);
                try {
                    Gson gson = new Gson();
                    String data = gson.toJson(mDataController.careGiver);

                    Log.d(TAG,"Updating with information... " + data);
                    backend.updateEntry(mRegistrationID, mEmail, data).execute();
                } catch (IOException e) {
                    Log.d(TAG, "updatedAccountInfo failed");
                    e.printStackTrace();
                }
            } else {
                Log.d(TAG, "Cannot update account because device is unregistered");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void arg) {
            Log.d(TAG,"Finished CareGiver Updating Information");
        }
    }

    class SendMessageToPatientAsyncTask extends AsyncTask<Void,Void,Void> {
        private static final String TAG = "Message Patient";
        public String msg;

        @Override
        protected Void doInBackground(Void... params) {

            edu.cs65.caregiver.backend.messaging.Messaging.Builder builder =
                    new edu.cs65.caregiver.backend.messaging.Messaging
                            .Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null)
                            .setRootUrl(SERVER_ADDR + "/_ah/api/");

            edu.cs65.caregiver.backend.messaging.Messaging backend = builder.build();

            if (mReceiverRegistered) {
                Log.d(TAG, "Sending message to recipient registered with email " + mEmail);
//                try {
//                    backend.sendNotificationToPatient(mRegistrationID, mEmail, msg).execute();
//                } catch (IOException e) {
//                    Log.d(TAG, "sendNotificationToPatient failed");
//                    e.printStackTrace();
//                }
            } else {
                Log.d(TAG, "Cannot sendNotificationToPatient because device is unregistered");
            }

            return null;
        }

    }

}
