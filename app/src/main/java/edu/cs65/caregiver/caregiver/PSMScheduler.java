package edu.cs65.caregiver.caregiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class PSMScheduler {

    // Method to set medication alarm
    public static void setSchedule(Context context) {

        // get times of all medications and set alarm
        for (int i = 0; i < CareRecipientActivity.sortedMeds.size(); i++) {
            Time alarmTime = CareRecipientActivity.sortedMeds.get(i).time;

            int hr = alarmTime.getHours();
            int min = alarmTime.getMinutes();
            int sec = alarmTime.getSeconds();

            setSchedule(context, hr, min, sec, i);
        }

    }

    // Method to set Checkin alarm -- repeats daily
    public static void setCheckinAlarm(Context context, long time) {
        if (time == 0) {
            setSchedule(context, 10, 0, 0, -1);
        }else {
            Time checkinTime = new Time(time);
            int hr = checkinTime.getHours();
            int min = checkinTime.getMinutes();
            int sec = checkinTime.getSeconds();
            System.out.println("Set Check In Time: " + checkinTime.toString());

            setSchedule(context, hr, min, sec, -1);
        }
    }


    private static void setSchedule(Context context, int hour, int min, int sec, int index) {
        int requestCode = hour * 10000 + min * 100 + sec;
        Intent intent = new Intent(context, EMAAlarmReceiver.class);
        intent.putExtra("index", index);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, min);
        calendar.set(Calendar.SECOND, sec);

        if(calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DATE, 1);
        }

        //set repeating alarm for checkin, otherwise set one-time alarm for meds
        if (index != -1) {
            PendingIntent piMeds = PendingIntent.getBroadcast(context, requestCode, intent,
                    PendingIntent.FLAG_CANCEL_CURRENT); //set pending intent for EMAAlarmReceiver.
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(), piMeds);
        }

        else {
            PendingIntent piCheckIn = PendingIntent.getBroadcast(context, requestCode, intent,
                    PendingIntent.FLAG_CANCEL_CURRENT); //set pending intent for EMAAlarmReceiver.
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, piCheckIn);
        }
    }
}
