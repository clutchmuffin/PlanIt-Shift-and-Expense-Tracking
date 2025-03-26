package com.example.myapplication.controller;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.model.AlarmReceiver;
import com.example.myapplication.model.AlarmSounder;
import com.example.myapplication.model.RingtoneHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class AlarmActivity extends AppCompatActivity {
    private FloatingActionButton dismissButton;
    private TextView tvJobName, tvShiftName, tvShiftStart, tvShiftEnd;
    private String jobName, shiftName, shiftStart, shiftEnd;
    private int ID;
    private AlarmManager manager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        initialize();
        populateDetails();
//        alertUser();
    }

    private void initialize(){
        dismissButton = findViewById(R.id.dismissButton);
        dismissButton.setOnClickListener(l -> dismissAlarm());

        tvJobName = findViewById(R.id.tvAlarmJob);
        tvShiftName = findViewById(R.id.tvAlarmName);
        tvShiftStart = findViewById(R.id.tvAlarmStart);
        tvShiftEnd = findViewById(R.id.tvAlarmEnd);


        Intent shiftInfo = getIntent();
        jobName = shiftInfo.getStringExtra("jobName");
        shiftName = shiftInfo.getStringExtra("shiftName");
        shiftStart = shiftInfo.getStringExtra("startTime");
        shiftEnd = shiftInfo.getStringExtra("endTime");
        ID = shiftInfo.getIntExtra("alarmID", ID);
    }

    private void populateDetails(){
        tvJobName.setText(jobName);
        tvShiftName.setText(shiftName);
        tvShiftStart.setText(shiftStart);
        tvShiftEnd.setText(shiftEnd);
    }

    private void alertUser(){
        Intent intent = new Intent(this, AlarmSounder.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        manager.setRepeating(AlarmManager.RTC_WAKEUP,System.currentTimeMillis(), 10000, pendingIntent);
    }

    private void dismissAlarm(){
//        Intent intent = new Intent(this, AlarmSounder.class);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
//        manager.cancel(pendingIntent);

        NotificationManager notifManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Intent notification = new Intent(this, AlarmReceiver.class);
        PendingIntent notificationPending = PendingIntent.getBroadcast(this,
                ID,
                notification,
                PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if(notificationPending != null) {
            notificationPending.cancel();
            notifManager.cancel(ID);
        }
        RingtoneHelper.stopRingtone();

        Intent mainIntent = new Intent(this,MainActivity.class);
        startActivity(mainIntent);
    }

}
