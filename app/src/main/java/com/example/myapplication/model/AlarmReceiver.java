package com.example.myapplication.model;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.AlarmClock;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String eventName = intent.getStringExtra("name");
        int hour = intent.getIntExtra("hour", 6);
        int minute = intent.getIntExtra("minute", 0);
        Intent alarmIntent = new Intent(AlarmClock.ACTION_SET_ALARM)
                .putExtra(AlarmClock.EXTRA_MESSAGE, eventName)
                .putExtra(AlarmClock.EXTRA_HOUR, hour)
                .putExtra(AlarmClock.EXTRA_MINUTES, minute)
                .putExtra(AlarmClock.EXTRA_VIBRATE,true)
                .putExtra(AlarmClock.EXTRA_SKIP_UI,true);
        if(alarmIntent.resolveActivity(context.getPackageManager()) != null)
            context.startActivity(alarmIntent);
    }
}
