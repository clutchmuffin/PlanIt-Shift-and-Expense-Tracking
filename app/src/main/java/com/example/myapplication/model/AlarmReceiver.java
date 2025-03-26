package com.example.myapplication.model;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.AlarmClock;

import com.example.myapplication.controller.AlarmActivity;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent alarmIntent = new Intent(context, AlarmActivity.class);
        String jobName = intent.getStringExtra("jobName");
        String shiftName = intent.getStringExtra("shiftName");
        String shiftStart = intent.getStringExtra("startTime");
        String shiftEnd = intent.getStringExtra("endTime");

        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        alarmIntent.putExtra("jobName", jobName);
        alarmIntent.putExtra("shiftName", shiftName);
        alarmIntent.putExtra("startTime", shiftStart);
        alarmIntent.putExtra("endTime", shiftEnd);


        context.startActivity(alarmIntent);
    }
}
