package com.example.scheduler;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.Calendar;

/**
 * Created by Srdjan on 24-Mar-18.
 */

public class AlarmBootReceiver extends BroadcastReceiver {

    private SharedPreferences sharedPreferences;
    private boolean setFlag;
    private AlarmManager alarmManager;

    @Override
    public void onReceive(Context context, Intent intent) {

         sharedPreferences = context.getSharedPreferences("com.example.scheduler", Context.MODE_PRIVATE);
         setFlag = sharedPreferences.getBoolean("setFlag",false);

         if (setFlag) {

             alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

             Calendar calendar = Calendar.getInstance();
             int hours = sharedPreferences.getInt("setHours", calendar.get(Calendar.HOUR_OF_DAY));
             int minutes = sharedPreferences.getInt("setMinutes", calendar.get(Calendar.MINUTE));
             calendar.setTimeInMillis(System.currentTimeMillis());
             calendar.set(Calendar.HOUR_OF_DAY, hours);
             calendar.set(Calendar.MINUTE, minutes);

             Intent alarmIntent = new Intent(context, AlarmReceiver.class);
             PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
             alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
             //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 1000 * 60 * repeatIntervalMinutes, pendingIntent);
         }
    }
}
