package edu.cs65.caregiver.caregiver.model;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.Vibrator;
import android.util.Log;

/**
 * Created by McFarland on 4/18/16.
 */
public class MyAlert {

    private final static String TAG = "MY_ALERT";

    private Vibrator mVibrator;
    private MediaPlayer mPlayer;

    private static final long v_pattern[] = {500, 500};

    public MyAlert(Context c) {
        mVibrator = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
        mPlayer = MediaPlayer.create(c, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
    }

    public void startAlarms() {
        Log.d(TAG,"starting alarms");
        mVibrator.vibrate(v_pattern, 0);
        mPlayer.start();
    }

    public void stopAlarms() {
        Log.d(TAG, "stopping alarms");
        mVibrator.cancel();

        if (mPlayer.isPlaying()) {
            mPlayer.stop();
            mPlayer.reset();
        }
    }
}
