package edu.cs65.caregiver.caregiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;


public class EMAAlarmReceiver extends BroadcastReceiver {

    //Receive broadcast
    @Override
    public void onReceive(final Context context, Intent intent) {
        int index = intent.getExtras().getInt("index");
        System.out.println("starting PSM...");
        startPSM(context, index);
    }

    // start CareRecipientActivity and load med dialog
    private void startPSM(Context context, int i) {

        // CareRecipientActivity
        if (i != -1) {
            System.out.println("Trying to start CareRecipientActivity...");

            Intent emaIntent = new Intent(context, CareRecipientActivity.class);
            emaIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            emaIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            CareRecipientActivity.loadMedDialog = true;
            emaIntent.putExtra("index", i);

            Intent notifyTakeMeds = new Intent("edu.cs65.caregiver.caregiver.CARERECIPIENT_BROADCAST");
            notifyTakeMeds.putExtra("take meds", true);
            notifyTakeMeds.putExtra("index",i);
            context.sendBroadcast(notifyTakeMeds);
            
            context.startActivity(emaIntent);
        }

        // Checkin activity
        else {
            Intent emaIntent = new Intent(context, Checkin.class);
            emaIntent.putExtra("registration", CareRecipientActivity.mRegistrationID);
            emaIntent.putExtra("email", CareRecipientActivity.mEmail);
            emaIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(emaIntent);
        }

    }
}
