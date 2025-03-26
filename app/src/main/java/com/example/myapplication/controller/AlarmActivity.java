package com.example.myapplication.controller;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class AlarmActivity extends AppCompatActivity {
    private FloatingActionButton dismissButton;
    private TextView tvJobName, tvShiftName, tvShiftStart, tvShiftEnd;
    private String jobName, shiftName, shiftStart, shiftEnd;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        System.out.println("Launching alarm");
        initialize();
//        populateDetails();
    }

    private void initialize(){
        dismissButton = findViewById(R.id.dismissButton);
        tvJobName = findViewById(R.id.tvAlarmJob);
        tvShiftName = findViewById(R.id.tvAlarmName);
        tvShiftStart = findViewById(R.id.tvAlarmStart);
        tvShiftEnd = findViewById(R.id.tvAlarmEnd);


        Intent shiftInfo = getIntent();
        jobName = shiftInfo.getStringExtra("jobName");
        shiftName = shiftInfo.getStringExtra("shiftName");
        shiftStart = shiftInfo.getStringExtra("startTime");
        shiftEnd = shiftInfo.getStringExtra("endTime");
    }

    public void populateDetails(){
        tvJobName.setText(jobName);
        tvShiftName.setText(shiftName);
        tvShiftStart.setText(shiftStart);
        tvShiftEnd.setText(shiftEnd);
    }
}
