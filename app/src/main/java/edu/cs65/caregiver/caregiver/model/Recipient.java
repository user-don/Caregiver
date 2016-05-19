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
    public boolean mCheckedIn;
    public boolean mRaisedAlert;

    public Recipient(String _name, ArrayList<MedicationAlert> _alerts) {
        mName = _name;

        if (_alerts == null) {
            mAlerts = new ArrayList<>();
        } else {
            mAlerts = _alerts;
        }

        mCheckedIn = true;
        mRaisedAlert = false;
    }

    public void addAlert(MedicationAlert alert) {
        mAlerts.add(alert);
        Collections.sort(mAlerts);
    }
}
