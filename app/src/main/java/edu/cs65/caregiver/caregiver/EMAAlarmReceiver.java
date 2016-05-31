package edu.cs65.caregiver.caregiver;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;


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
        Intent emaIntent;
        String message;

        // CareRecipientActivity
        if (i != -1) {
            System.out.println("Trying to start CareRecipientActivity...");

            message = "Time to take your medications!";
            emaIntent = new Intent(context, CareRecipientActivity.class);
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
            message = "Time to check-in!";

            emaIntent = new Intent(context, Checkin.class);
            emaIntent.putExtra("registration", CareRecipientActivity.mRegistrationID);
            emaIntent.putExtra("email", CareRecipientActivity.mEmail);
            emaIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            emaIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(emaIntent);
        }

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */,
                emaIntent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.logo_white)
                .setContentTitle("CareGiver Message")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());




    }
}
