package edu.cs65.caregiver.caregiver;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;

import java.util.logging.Level;
import java.util.logging.Logger;

import edu.cs65.caregiver.caregiver.model.CareGiver;
import edu.cs65.caregiver.caregiver.model.MedicationAlert;
import edu.cs65.caregiver.caregiver.model.MyAlert;
import edu.cs65.caregiver.caregiver.model.Recipient;
import edu.cs65.caregiver.caregiver.model.RecipientToCareGiverMessage;


/**
 * Called when the app receives a message from GCM. For now, this is exclusively
 * delete requests.
 */
public class GcmIntentService extends IntentService {

    private static final String TAG = "GCMIntentService";
    Gson gson = new Gson();

    private SharedPreferences prefs;
    private String mRecipientName;

    public static final String BROADCAST_MESSAGE = "broadcast message";

    public GcmIntentService() {

        super("GcmIntentService");

        mRecipientName = CareRecipientActivity.mRecipientName;
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.d(TAG, "Received GCM Intent");

        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        prefs = getSharedPreferences(getString(R.string.profile_preference), 0);
        mRecipientName = prefs.getString(CareGiverActivity.RECIPIENT_NAME_KEY,"");

        if (extras != null && !extras.isEmpty()) {  // has effect of unparcelling Bundle
            if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                Logger.getLogger("GCM_RECEIVED").log(Level.INFO, extras.toString());

                String msg = "Message from Cloud: " + extras.getString("message");
                RecipientToCareGiverMessage data = gson.fromJson(extras.getString("message"), RecipientToCareGiverMessage.class);

                switch (data.messageType) {
                    case RecipientToCareGiverMessage.CHECKIN:
                        //showToast("Recipient Checked In!");
                        sendCareGiverNotification(mRecipientName + " Checked In!");

                        Intent i1 = new Intent("edu.cs65.caregiver.caregiver.CAREGIVER_BROADCAST");
                        i1.putExtra("msg", extras.getString("message"));
                        sendBroadcast(i1);
                        break;

                    case RecipientToCareGiverMessage.HELP:

                        sendCareGiverNotification(mRecipientName + " NEEDS HELP!");

                        if (CareGiverActivity.mAlert != null) {
                            CareGiverActivity.mAlert.startAlarms();
                        }

                        Intent i2 = new Intent("edu.cs65.caregiver.caregiver.CAREGIVER_BROADCAST");
                        i2.putExtra("msg", extras.getString("message"));
                        sendBroadcast(i2);

                        PowerManager.WakeLock screenOn = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "ALERT");
                        screenOn.acquire();

                        prefs.edit().putBoolean("help", true).commit();
                        break;

                    case RecipientToCareGiverMessage.MED_TAKEN:
                        StringBuilder sb = new StringBuilder(100);
                        for (String alert : data.medAlertNames) {
                            sb.append(alert + ", ");
                        }
                        sendCareGiverNotification(mRecipientName + " Took Meds: " + sb.toString());

                        Intent i3 = new Intent("edu.cs65.caregiver.caregiver.CAREGIVER_BROADCAST");
                        i3.putExtra("msg", extras.getString("message"));
                        sendBroadcast(i3);
                        break;

                    case RecipientToCareGiverMessage.MED_NOT_TAKEN:
                        StringBuilder sb2 = new StringBuilder(100);
                        for (String alert : data.medAlertNames) {
                            sb2.append(alert + ", ");
                        }
                        sendCareGiverNotification(mRecipientName + " Hasn't Taken: " + sb2.toString());

                        Intent i4 = new Intent("edu.cs65.caregiver.caregiver.CAREGIVER_BROADCAST");
                        i4.putExtra("msg", extras.getString("message"));
                        sendBroadcast(i4);
                        break;

                    case RecipientToCareGiverMessage.UPDATE_INFO:
                        // launch intent to send information to CareRecipient activity
                        Intent i5 = new Intent("edu.cs65.caregiver.caregiver.CARERECIPIENT_BROADCAST");
                        i5.putExtra("msg", extras.getString("message"));
                        sendBroadcast(i5);
                        break;

                    default:
                        Log.d(TAG, "unrecognized message");
                }
            }
        }
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    protected void showToast(final String message) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void mySendBroadcast(String action, String message)  {
        Log.d(TAG, "sending broadcast: " + action);
        Intent i = new Intent(action);
        i.putExtra(BROADCAST_MESSAGE, message);
        sendBroadcast(i);
    }

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendCareGiverNotification(String message) {
        Intent intent = new Intent(this, CareGiverActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_done_black_24dp)
                .setContentTitle("CareGiver Message")
                .setContentText(message)
                .setOngoing(true)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}