package com.example.myapplication.model;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;


public class Notification {
    private Context context;
    public static String channel_name = "dailyNotif";
    public static String channel_desc = "a notification channel that gets sent for every shift";

    public Notification(Context context) {
        this.context = context;
    }

    public void showNotification(CalendarEvent event){
        String name = event.getName();
        String startTime = event.getBegin_time();
        String endTime = event.getEnd_time();
        int ID = event.getID();

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("name", name);
        intent.putExtra("startTime", startTime);
        intent.putExtra("endTime", endTime);
        intent.putExtra("ID", ID);

        PendingIntent pendintent = PendingIntent.getBroadcast(context,
                ID,
                intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0);

        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        manager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + 5000,
                pendintent);
    }
}