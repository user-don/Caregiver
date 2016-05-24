package edu.cs65.caregiver.caregiver;

import android.app.DialogFragment;
import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
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
    private ArrayList<MedEntry> sortedMeds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        PSMScheduler.setSchedule(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_care_recipient);

        if (mCareGiver == null) {
            mCareGiver = new CareGiver("test");
            mReceiver = mCareGiver.addRecipient("test recipient");
        } else {
            mReceiver = mCareGiver.getRecipient("test recipient");
        }

        getDayOfWeek();

        // Get medication alerts and checkin time
        mMedicationAlerts = mReceiver.mAlerts;
        mCheckInTime = mReceiver.mCheckIntime;

        setUpAdapter();
    }

    private void getDayOfWeek() {
        Calendar calendar = Calendar.getInstance();
        mDay = calendar.get(Calendar.DAY_OF_WEEK);
        dayIndex = mDay - 1; // convert to match MedicationAlert int values
    }

    @Override
    protected void onListItemClick(ListView list, View view, int position, long id) {
        super.onListItemClick(list, view, position, id);

        String selectedItem = (String) getListView().getItemAtPosition(position);
        switch (selectedItem) {
            case "10am":
                displayDialog(CareGiverDialogFragment.DIALOG_10AM);
                break;
            case "1pm":
                displayDialog(CareGiverDialogFragment.DIALOG_1PM);
                break;
            case "6pm":
                displayDialog(CareGiverDialogFragment.DIALOG_6PM);
                break;
        }
    }

    public void displayDialog(int id) {
        DialogFragment fragment = CareGiverDialogFragment.newInstance(id);
        fragment.show(getFragmentManager(),
                getString(R.string.app_name));
    }

    public void onHelpClicked(View v) {
        Toast.makeText(getApplicationContext(), "CAREGIVER HAS BEEN ALERTED",
                Toast.LENGTH_LONG).show();
    }

    public void onMenuClicked(View v) {
        displayDialog(CareGiverDialogFragment.DIALOG_MENU);
    }

    public void setUpAdapter() {
        ArrayList<MedicationAlert> todaysMeds = getMedsForToday();

        sortedMeds = getGroupedMeds(todaysMeds);

        listValues = new ArrayList<String>();
        listValues.add("10am");
        listValues.add("1pm");
        listValues.add("6pm");

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
            if (alertDays[7] != 0 || alertDays[dayIndex] != 0) {
                medsForToday.add(mMedicationAlerts.get(i));
            }
        }

        return medsForToday;
    }

    public ArrayList<MedEntry> getGroupedMeds(ArrayList<MedicationAlert> todaysMeds) {
        ArrayList<MedEntry> sortedMeds = new ArrayList<>();

        for (int i = 0; i < todaysMeds.size(); i++) {
            for (int j = i+1; j < todaysMeds.size(); j++) {
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
                // associated with that time
                else {
                    for (int k = 0; k < todaysMeds.get(j).mMedications.size(); k++) {
                        sortedMeds.get(i).addMedToEntry(todaysMeds.get(j).mMedications.get(k));
                    }
                }
            }
        }

        return sortedMeds;
    }



    public class MedEntry {
        private final String label;
        private ArrayList<String> meds;
        private final Time time;

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
