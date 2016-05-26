package edu.cs65.caregiver.caregiver.model;

import java.util.ArrayList;

/**
 * Created by McFarland on 5/18/16.
 */
public class CareGiver {
    public String mUserName;
    //public String mUserEmail;

    private int mCloudID;

    public ArrayList<Recipient> mRecipients;

    public CareGiver(String _username) {
        mUserName = _username;
        mRecipients = new ArrayList<Recipient>();
        mCloudID = -1;
    }

    public Recipient addRecipient(String _name) {
        Recipient newRecipient = new Recipient(_name, new ArrayList<MedicationAlert>());
        mRecipients.add(newRecipient);
        return newRecipient;
    }

    public Recipient getRecipient(String _name) {
        for (int i = 0; i < mRecipients.size(); i++){
            if (mRecipients.get(i).mName.equals(_name))
                return mRecipients.get(i);
        }

        return null;
    }

    public void deleteRecipient(String name) {
        for (Recipient recipient : mRecipients) {
            if (recipient.mName.equals(name)) {
                mRecipients.remove(recipient);
            }
        }
    }

}
