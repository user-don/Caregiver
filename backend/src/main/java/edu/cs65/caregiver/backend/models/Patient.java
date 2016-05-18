package edu.cs65.caregiver.backend.models;

import com.google.appengine.repackaged.com.google.api.client.util.ArrayMap;
import com.google.appengine.repackaged.com.google.common.collect.HashMultimap;
import com.google.appengine.repackaged.com.google.common.collect.Multiset;

import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by don on 5/18/16.
 */
public class Patient {

    private String patientName;
    // Check-in variables
    private DateTime checkInTime;
    private Boolean checkedIn;
    // Medication
    private HashMultimap<String, MedTime> medTimes;
    // Track if fallen
    private Boolean hasFallen;

    public Patient(DateTime checkInTime, Boolean checkedIn,
                   HashMultimap<String, MedTime> medTimes, String patientName) {
        this.checkInTime = checkInTime;
        this.checkedIn = checkedIn;
        this.medTimes = medTimes;
        this.hasFallen = false;
        this.checkedIn = false;
        this.patientName = patientName;
    }

    public DateTime getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(DateTime checkInTime) {
        this.checkInTime = checkInTime;
    }

    public Boolean getCheckedIn() {
        return checkedIn;
    }

    public void setCheckedIn(Boolean checkedIn) {
        this.checkedIn = checkedIn;
    }

    public HashMultimap<String, MedTime> getMedTimes() {
        return medTimes;
    }

    public void setMedTimes(HashMultimap<String, MedTime> medTimes) {
        this.medTimes = medTimes;
    }

    public Boolean getHasFallen() {
        return hasFallen;
    }

    public void setHasFallen(Boolean hasFallen) {
        this.hasFallen = hasFallen;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }
}
