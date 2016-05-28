package edu.cs65.caregiver.caregiver.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.sql.Time;
import java.util.ArrayList;

/**
 * Created by McFarland on 5/18/16.
 */
public class MedicationAlert implements Comparable<MedicationAlert> {
    public String mName;
    public Time mTime;
    public int[] mAlertDays;
    public boolean mMedsTaken;
    public ArrayList<String> mMedications;

    public MedicationAlert(String _name, Time _time, int[] _days, boolean _medsTaken, ArrayList<String> _medications) {
        mName = _name;
        mTime = _time;
        mAlertDays = _days;
        mMedsTaken = _medsTaken;
        mMedications = _medications;
    }

    @Override
    public int compareTo(MedicationAlert other) {
        return Long.compare(mTime.getTime(), other.mTime.getTime());
    }

}
