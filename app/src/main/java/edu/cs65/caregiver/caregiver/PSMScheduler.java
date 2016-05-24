package edu.cs65.caregiver.caregiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.sql.Time;
import java.util.Calendar;


public class PSMScheduler {

    public static void setSchedule(Context context) {

        // get times of all medications
        for (int i = 0; i < CareRecipientActivity.sortedMeds.size(); i++) {
            Time alarmTime = CareRecipientActivity.sortedMeds.get(i).time;

            int hr = alarmTime.getHours();
            int min = alarmTime.getMinutes();
            int sec = alarmTime.getSeconds();

            setSchedule(context, hr, min, sec, i);
        }
    }

    private static void setSchedule(Context context, int hour, int min, int sec, int index) {

        // the request code distinguish different stress meter schedule instances
        int requestCode = hour * 10000 + min * 100 + sec;
        Intent intent = new Intent(context, EMAAlarmReceiver.class);
        intent.putExtra("index", index);

        PendingIntent pi = PendingIntent.getBroadcast(context, requestCode, intent,
        PendingIntent.FLAG_CANCEL_CURRENT); //set pending intent to call EMAAlarmReceiver.

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, min);
        calendar.set(Calendar.SECOND, sec);

        if(calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DATE, 1);
        }

        //set repeating alarm, and pass the pending intent,
        //so that the broadcast is sent everytime the alarm
        // is triggered
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pi);
    }
}
