package edu.cs65.caregiver.backend.models;

import com.google.common.base.Objects;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import java.util.ArrayList;

/**
 * Object for each medication time
 *
 * Created by don on 5/18/16.
 */
public class MedTime {

    private LocalTime time;
    private ArrayList<Boolean> days;
    private ArrayList<String> medications;
    private int id;

    /**
     * Construct a MedTime object. This object represents a point during the day
     * where the patient must take their medication.
     * @param time time of day to take medication
     * @param days days of week to take medication
     * @param medications ordered list of medications to take at the given time
     */
    public MedTime(LocalTime time, ArrayList<Boolean> days, ArrayList<String> medications, int id) {
        this.time = time;
        this.days = days;
        this.medications = medications;
        this.id = id;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public ArrayList<Boolean> getDays() {
        return days;
    }

    public void setDays(ArrayList<Boolean> days) {
        this.days = days;
    }

    public ArrayList<String> getMedications() {
        return medications;
    }

    public void setMedications(ArrayList<String> medications) {
        this.medications = medications;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MedTime medTime = (MedTime) o;
        return Objects.equal(time, medTime.time) &&
                Objects.equal(days, medTime.days) &&
                Objects.equal(medications, medTime.medications);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(time, days, medications);
    }
}
