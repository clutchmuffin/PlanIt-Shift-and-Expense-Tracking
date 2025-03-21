package com.example.myapplication.model;

import static android.content.ContentValues.TAG;
import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.myapplication.R;
import com.example.myapplication.controller.MainActivity;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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


        // Build notification for weekly notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NotificationSender.weekly_channel_name)
                .setSmallIcon(R.drawable.baseline_add_alert_24)
                .setContentIntent(pendingIntent)
                .setContentTitle("Weekly Summary")
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        long nextSunday = intent.getLongExtra("nextSunday", 0);
        long twoWeekSunday = nextSunday + intent.getLongExtra("interval", 0);
        SharedPreferences prefs = context.getSharedPreferences("PlanITPrefs", MODE_PRIVATE);
        String currentUserId = prefs.getString("userId", null);


        if (nextSunday != 0) {
            StringBuilder content = new StringBuilder("");
            // Gets all events that fall within the next two Sundays
            db.collection("Jobs")
                    .whereEqualTo("userId", currentUserId) // Get only jobs owned by current user
                    .get()
                    .addOnCompleteListener(jobTask -> {
                        if (jobTask.isSuccessful()) {
                            Log.i(TAG, "Fetched Jobs for User");
                            List<Task<QuerySnapshot>> eventTasks = new ArrayList<>();

                            for (QueryDocumentSnapshot jobDoc : jobTask.getResult()) {
                                Task<QuerySnapshot> eventTask = jobDoc.getReference().collection("Events")
                                        .get();
                                eventTasks.add(eventTask);
                            }

                            Tasks.whenAllComplete(eventTasks).addOnCompleteListener(allEventsTask -> {

                                for (Task<QuerySnapshot> eventTask : eventTasks) {
                                    if (eventTask.isSuccessful()) {
                                        for (QueryDocumentSnapshot eventDoc : eventTask.getResult()) {
                                            CalendarEvent event = eventDoc.toObject(CalendarEvent.class);
                                            Log.i(TAG, "Checking if " + event.getName() + " falls within next two weeks");
                                            if (fallsWithinWeeks(event, nextSunday, twoWeekSunday)) {
                                                content.append(event.getName() + "\nOn: "+ event.getBegin_date() + " " + event.getBegin_time() + "\nUntil: " + event.getEnd_date() + " " + event.getEnd_time() + "\n");
                                            }
                                        }
                                    }
                                }
                                if(content.isEmpty()){
                                    builder.setContentText("No shifts this week");
                                }
                                else{
                                    builder.setContentText("Here are your shifts for the next week");
                                    builder.setStyle((new NotificationCompat.BigTextStyle()).bigText(content));
                                }

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                        ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
                                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                                        notificationManager.notify(2, builder.build());
                                    } else {
                                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                                        notificationManager.notify(2, builder.build());
                                    }
                                }
                            });
                        }
                    });
        }
    }
    /**
     * Gets the time in milliseconds from epoch form given String date and hour/minute
     * @param date --> the date to convert
     * @param hour --> the hour to convert
     * @param minute --> the minute to convert
     * @return The time in millisecond from epoch to date, time:hour
     */
    private long getMilliDateTime(String date, int hour, int minute){
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(date, dateFormatter);
        ZonedDateTime dateTime = localDate.atTime(hour, minute).atZone(TimeZone.getDefault().toZoneId());

        return dateTime.toInstant().toEpochMilli();
    }

    /**
     * Checks if a calender event falls between firstWeek and secondWeek
     * @param event --> the event to check
     * @param firstWeek --> the millisecond time stamp of the first week
     * @param secondWeek --> the millisecond time stamp of the second week
     * @return true if event falls between firstWeek and secondWeek, false otherwise
     */
    private boolean fallsWithinWeeks(CalendarEvent event, long firstWeek, long secondWeek){
        LocalTime time = LocalTime.parse(event.getBegin_time(), DateTimeFormatter.ofPattern("HH:mm:ss"));

        String date = event.getBegin_date();
        int hour = time.getHour();
        int minute = time.getMinute();
        long milliDate = getMilliDateTime(date,hour,minute);

        if(firstWeek <= milliDate && secondWeek >= milliDate){
            return true;
        }
        else
            return false;
    }



}

