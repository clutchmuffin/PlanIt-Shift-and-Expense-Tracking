package com.example.myapplication.controller;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.model.AlarmReceiver;

import com.example.myapplication.model.RingtoneHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class AlarmActivity extends AppCompatActivity {
    private FloatingActionButton dismissButton;
    private TextView tvJobName, tvShiftName, tvShiftStart, tvShiftEnd;
    private String jobName, shiftName, shiftStart, shiftEnd, endDate;
    private int ID;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        initialize();
        populateDetails();
    }

    /**
     * Initialize all parts of the content view that will be changed
     */
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
        endDate = shiftInfo.getStringExtra("endDate");
        ID = shiftInfo.getIntExtra("alarmID", ID);
    }

    /**
     * Adds details to the alarm screen so the user knows what they are doing for that shift
     */
    private void populateDetails(){
        tvJobName.setText(jobName);
        tvShiftName.setText(shiftName);
        tvShiftStart.setText("Starts today at: " + shiftStart);
        tvShiftEnd.setText("Ends " + endDate + " at: " + shiftEnd);
    }

    /**
     * Dismisses the alarm and sends the user to MainActivity
     */
    private void dismissAlarm(){
        // Cancel alarm notification
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
        // Stop the user's ringtone
        RingtoneHelper.stopRingtone();

        // When the user clicks dismiss, launch the Main Activity
        Intent mainIntent = new Intent(this,MainActivity.class);
        startActivity(mainIntent);
    }

}
