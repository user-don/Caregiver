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
        startPSM(context, index);
    }

    // start CareRecipientActivity and load med dialog
    private void startPSM(Context context, int i) {
        Intent emaIntent = new Intent(context, CareRecipientActivity.class); //The activity you  want to start.
        emaIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        CareRecipientActivity.loadMedDialog = true;

        emaIntent.putExtra("index", i);
        context.startActivity(emaIntent);
    }
}
