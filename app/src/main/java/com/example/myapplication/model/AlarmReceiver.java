package com.example.myapplication.model;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.myapplication.R;
import com.example.myapplication.controller.AlarmActivity;


public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Pass on information that the alarm screen will need
        String jobName = intent.getStringExtra("jobName");
        String shiftName = intent.getStringExtra("shiftName");
        String shiftStart = intent.getStringExtra("startTime");
        String shiftEnd = intent.getStringExtra("endTime");
        String endDate = intent.getStringExtra("endDate");
        String inHours = intent.getStringExtra("inHours");
        int ID = intent.getIntExtra("alarmID", 0);

        Intent alarmIntent = new Intent(context, AlarmActivity.class);
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        alarmIntent.putExtra("jobName", jobName);
        alarmIntent.putExtra("shiftName", shiftName);
        alarmIntent.putExtra("startTime", shiftStart);
        alarmIntent.putExtra("endTime", shiftEnd);
        alarmIntent.putExtra("endDate", endDate);
        alarmIntent.putExtra("alarmID", ID);

        // Play phone's ringtone to wake up the user
        RingtoneHelper.playRingtone(context);

        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                0,
                alarmIntent,
                PendingIntent.FLAG_IMMUTABLE);

        // The notification that will be used to wake the user and allow them to tap on it which sends them to the alarm screen
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NotificationSender.alarm_channel)
                .setSmallIcon(R.drawable.baseline_add_alert_24)
                .setContentIntent(pendingIntent)
                .setContentTitle("Shift in: " + inHours)
                .setAutoCancel(true)
                .setVibrate(new long[]{1000, 1000, 1000})
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // Check if Notification Permissions were given, and ask if they weren't
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                notificationManager.notify(ID, builder.build());
            } else {
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                notificationManager.notify(ID, builder.build());
            }
        }
    }
}
