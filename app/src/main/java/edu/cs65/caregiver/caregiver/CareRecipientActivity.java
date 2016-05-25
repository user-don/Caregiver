package edu.cs65.caregiver.caregiver;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import edu.cs65.caregiver.caregiver.model.CareGiver;
import edu.cs65.caregiver.caregiver.model.MedicationAlert;
import edu.cs65.caregiver.caregiver.model.Recipient;


public class CareRecipientActivity extends ListActivity {

    // Menu Options
    private static final int ACCT = 0;
    private static final int FALL = 1;
    private static final int CHECK_IN = 2;

    private CareGiver mCareGiver;
    private Recipient mReceiver;
    private long mCheckInTime;
    private int mDay;
    private int dayIndex;

    private List<String> listValues;
    private ArrayList<MedicationAlert> mMedicationAlerts;

    public static ArrayList<MedEntry> sortedMeds;
    public static MediaPlayer mediaPlayer;
    public static Vibrator v;
    public static boolean loadMedDialog = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_care_recipient);

        if (mCareGiver == null) {
            mCareGiver = new CareGiver("test");
            mReceiver = mCareGiver.addRecipient("test recipient");
        } else {
            mReceiver = mCareGiver.getRecipient("test recipient");
        }

        getDayOfWeek();
        createTestMeds();

        // Get medication alerts and checkin time
        mMedicationAlerts = mReceiver.mAlerts;
        mCheckInTime = mReceiver.mCheckIntime;

        setUpAdapter();

        if (loadMedDialog) {
            startAlarm();
            int index = getIntent().getExtras().getInt("index");
            MedEntry entry = sortedMeds.get(index);
            displayMedDialog(entry);
        }

        PSMScheduler.setSchedule(this);

    }


    public void getDayOfWeek() {
        Calendar calendar = Calendar.getInstance();
        mDay = calendar.get(Calendar.DAY_OF_WEEK);
        dayIndex = mDay - 1; // convert to match MedicationAlert int values
    }

    @Override
    protected void onListItemClick(ListView list, View view, int position, long id) {
        super.onListItemClick(list, view, position, id);
        displayMedDialog(sortedMeds.get(position));
    }

    public void displayMedDialog(MedEntry entry) {
        final ArrayList selectedItems = new ArrayList();
        String meds[] = new String[entry.meds.size()];
        meds = entry.meds.toArray(meds);

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle(entry.label);
        alertBuilder.setMultiChoiceItems(meds, null, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {

                if (isChecked) {
                    selectedItems.add(which);
                } else if (selectedItems.contains(which)) {
                    selectedItems.remove(Integer.valueOf(which));
                }

            }
        }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // notify CareGiver that medicine was taken
                stopAlarm();
                loadMedDialog = false;

            }
        }).setNegativeButton("Back", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                stopAlarm();
                loadMedDialog = false;

            }
        }).show();

    }

    public void onHelpClicked(View v) {
        Toast.makeText(getApplicationContext(), "CAREGIVER HAS BEEN ALERTED",
                Toast.LENGTH_LONG).show();
    }

    public void onMenuClicked(View v) {
//        displayMedDialog(CareGiverDialogFragment.DIALOG_MENU);
    }

    public void setUpAdapter() {
        ArrayList<MedicationAlert> todaysMeds = getMedsForToday();
        sortedMeds = getGroupedMeds(todaysMeds);
        listValues = new ArrayList<String>();

        for (int i = 0; i < sortedMeds.size(); i++) {
            String title = convertTime(sortedMeds.get(i).time);
            sortedMeds.get(i).label = title;
            listValues.add(title + " Medications");
        }

        // initiate list adapter
        ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(this, R.layout.row_layout,
                R.id.listText, listValues);

        // assign the list adapter
        setListAdapter(myAdapter);
    }

    public ArrayList<MedicationAlert> getMedsForToday() {
        ArrayList<MedicationAlert> medsForToday = new ArrayList<>();

        for (int i = 0; i < mMedicationAlerts.size(); i++) {
            int[] alertDays = mMedicationAlerts.get(i).mAlertDays;
            if (alertDays[dayIndex] != 0) {
                medsForToday.add(mMedicationAlerts.get(i));
            }
        }

        return medsForToday;
    }

    public ArrayList<MedEntry> getGroupedMeds(ArrayList<MedicationAlert> todaysMeds) {
        ArrayList<MedEntry> sortedMeds = new ArrayList<>();
        for (int i = 0; i < todaysMeds.size()-1; i++) {
            int j = i+1;
            // automatically add first medication
            if (i == 0) {
                MedEntry newEntry = new MedEntry(todaysMeds.get(i).mTime.toString(),
                        todaysMeds.get(i).mMedications, todaysMeds.get(i).mTime);
                sortedMeds.add(newEntry);
            }
            // compare medication alert times
            if (todaysMeds.get(i).compareTo(todaysMeds.get(j)) != 0) {
                MedEntry entry = new MedEntry(todaysMeds.get(j).mTime.toString(),
                        todaysMeds.get(j).mMedications, todaysMeds.get(j).mTime);
                sortedMeds.add(entry);
            }
            // if found duplicate time, append medications to existing MedEntry object
            else {
                for (int k = 0; k < todaysMeds.get(j).mMedications.size(); k++) {
                    sortedMeds.get(i).addMedToEntry(todaysMeds.get(j).mMedications.get(k));
                }
            }
        }

        return sortedMeds;
    }

    public String convertTime(Time time) {
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

        return convertedTime;
    }

    private void startAlarm() {
        mediaPlayer = MediaPlayer.create(this,
                RingtoneManager.getDefaultUri((RingtoneManager.TYPE_ALARM)));
        mediaPlayer.start();

        v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {0, 500, 1000};
        v.vibrate(pattern, 0);
    }

    public static void stopAlarm() {
        mediaPlayer.stop();
        v.cancel();
    }


    /* –––––––––––––––– Testing ONLY –––––––––––––––– */

    public void createTestMeds() {
        Time time1 = new Time(10, 0, 0);
        Time time2 = new Time(15, 15, 0);
        Time time3 = new Time(7, 53, 0); //10
        Time time4 = new Time(7, 15, 0); //20 / 8pm

        String name1 = "Test1";
        String name2 = "Test2";
        String name3 = "Test3";
        String name4 = "Test4";

        int[] days1 = new int[7];
        days1[0] = 0;
        days1[1] = 0;
        days1[2] = 1;
        days1[3] = 0;
        days1[4] = 0;
        days1[5] = 0;
        days1[6] = 1;

        int[] days2 = new int[7];
        days2[0] = 0;
        days2[1] = 0;
        days2[2] = 1;
        days2[3] = 0;
        days2[4] = 0;
        days2[5] = 0;
        days2[6] = 1;

        int[] days3 = new int[7];
        days3[0] = 1;
        days3[1] = 1;
        days3[2] = 1;
        days3[3] = 1;
        days3[4] = 1;
        days3[5] = 1;
        days3[6] = 1;

        ArrayList<String> meds1 = new ArrayList<>();
        meds1.add("Tylenol");
        meds1.add("Motrin");

        ArrayList<String> meds2 = new ArrayList<>();
        meds2.add("Benedryl");
        meds2.add("Mucinex");

        ArrayList<String> meds3 = new ArrayList<>();
        meds3.add("Valium");

        MedicationAlert medAlert1 = new MedicationAlert(name1, time1, days1, meds1);
        MedicationAlert medAlert2 = new MedicationAlert(name2, time2, days2, meds2);
        MedicationAlert medAlert3 = new MedicationAlert(name3, time3, days3, meds3);
        MedicationAlert medAlert4 = new MedicationAlert(name4, time4, days3, meds3);

        mReceiver.addAlert(medAlert1);
        mReceiver.addAlert(medAlert2);
        mReceiver.addAlert(medAlert3);
        mReceiver.addAlert(medAlert4);
    }


    public class MedEntry {
        public String label;
        public ArrayList<String> meds;
        public final Time time;

        public MedEntry(String name, ArrayList<String> meds, Time time) {
            this.label = name;
            this.meds = meds;
            this.time = time;
        }

        public void addMedToEntry(String newMed) {
            this.meds.add(newMed);
        }

    }

}
