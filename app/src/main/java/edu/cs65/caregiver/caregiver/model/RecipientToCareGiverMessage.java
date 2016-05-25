package edu.cs65.caregiver.caregiver.model;

import com.google.gson.Gson;

import java.util.ArrayList;

/**
 * Created by ellenli on 5/25/16.
 */
public class RecipientToCareGiverMessage {
    int CHECKIN = 0;
    int MED_TAKEN = 1;
    int MED_NOT_TAKEN = 2;
    int HELP = 3;

    public int messageType;
    public long time;
    public ArrayList<String> medAlertNames;

    public RecipientToCareGiverMessage(int messageType, ArrayList<String> alertNames, long time) {
        this.messageType = messageType;
        this.medAlertNames = alertNames;
        this.time = time;
    }

    public String selfToString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public void selfFromString(String string) {
        Gson gson = new Gson();
        RecipientToCareGiverMessage recData = gson.fromJson(string, RecipientToCareGiverMessage.class);

        this.messageType = recData.messageType;
        this.time = recData.time;
        this.medAlertNames = recData.medAlertNames;
    }

}
