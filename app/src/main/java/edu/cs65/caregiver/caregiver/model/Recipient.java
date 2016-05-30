package edu.cs65.caregiver.caregiver.model;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by McFarland on 5/18/16.
 */
public class Recipient {
    public String mName;
    public ArrayList<MedicationAlert> mAlerts;
    public long mCheckIntime;
    public boolean mCheckedIn;
    public boolean mRaisedAlert;

    /* daily status information */
    public ArrayList<Boolean> mHasTakenMed;
    public boolean mHasCheckedInToday;
    public long mCheckedInTime;

    public Recipient(String _name, ArrayList<MedicationAlert> _alerts) {
        mName = _name;

        if (_alerts == null) {
            mAlerts = new ArrayList<>();
        } else {
            mAlerts = _alerts;
        }

        //mCheckedIn = true;
        mRaisedAlert = false;

        mHasTakenMed = new ArrayList<>();
        mHasCheckedInToday = false;
        mCheckedInTime = -1;
    }

    public void addAlert(MedicationAlert alert) {
        mAlerts.add(alert);
        Collections.sort(mAlerts);
    }

    public void deleteAlert(String to_delete) {
        for (int i = 0; i < mAlerts.size(); i++) {
            if (mAlerts.get(i).mName.equals(to_delete)) {
                mAlerts.remove(i);
            }
        }
    }
}
