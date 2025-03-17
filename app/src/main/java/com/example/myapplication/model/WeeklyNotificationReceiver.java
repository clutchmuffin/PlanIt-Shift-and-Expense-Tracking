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

import org.checkerframework.checker.units.qual.C;

import java.util.Calendar;

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
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NotificationSender.channel_name)
                .setSmallIcon(R.drawable.baseline_add_alert_24)
                .setContentIntent(pendingIntent)
                .setContentTitle("Weekly Summary")
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

    db.collectionGroup("Events").get().addOnCompleteListener(task -> {
        if (task.isSuccessful()) {
            for (QueryDocumentSnapshot document : task.getResult()) {
                CalendarEvent otherEvent = document.toObject(CalendarEvent.class);
                Log.i("WeeklyNotificationReceiver", "Checking if " + otherEvent.getName() + " falls within next week");

                }
            }
        });
    }


}

