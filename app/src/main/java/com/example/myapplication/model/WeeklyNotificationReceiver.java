package com.example.myapplication.model;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.myapplication.R;
import com.example.myapplication.controller.MainActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class WeeklyNotificationReceiver extends BroadcastReceiver {
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public void onReceive(Context context, Intent intent) {
        // Makes PendingIntent for MainActivity so if someone clicks on the notification
        // it sends them to MainActivity
        Intent activityIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                1,
                activityIntent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0);


        // Build notification for daily shift
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NotificationSender.weekly_channel_name)
                .setSmallIcon(R.drawable.baseline_add_alert_24)
                .setContentIntent(pendingIntent)
                .setContentTitle("Weekly Summary")
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        long nextSunday = intent.getLongExtra("nextSunday",0);
        long twoWeekSunday = nextSunday + intent.getLongExtra("interval", 0);
        List<CalendarEvent> fallNextWeek = new ArrayList<>();

        if(nextSunday!= 0) {
            db.collectionGroup("Events").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        CalendarEvent event = document.toObject(CalendarEvent.class);
                        Log.i("WeeklyNotificationReceiver", "Checking if " + event.getName() + " falls within next two weeks");
                        if(fallsWithinWeeks(event, nextSunday, twoWeekSunday))
                            fallNextWeek.add(event);
                    }
                }
            });
        }


    }
    private long getMilliDateTime(String date, int hour, int minute){
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(date, dateFormatter);
        ZonedDateTime dateTime = localDate.atTime(hour, minute).atZone(TimeZone.getDefault().toZoneId());

        return dateTime.toInstant().toEpochMilli();
    }

    private boolean fallsWithinWeeks(CalendarEvent event, long firstWeek, long secondWeek){
        LocalTime time = LocalTime.parse(event.getBegin_time(), DateTimeFormatter.ofPattern("HH:mm"));

        String date = event.getBegin_date();
        int hour = time.getHour();
        int minute = time.getMinute();
        long milliDate = getMilliDateTime(date,hour,minute);

        if(firstWeek <= milliDate && milliDate <= secondWeek)
            return true;
        else
            return false;
    }



}

