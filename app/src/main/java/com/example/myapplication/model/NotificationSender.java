package com.example.myapplication.model;


import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.TimeZone;


public class NotificationSender {
    private final Context context;
    private final AlarmManager manager;
    public static String daily_channel_name = "dailyNotif";
    public static String daily_channel_desc = "a notification channel that gets used to send notifications for every shift";
    public static String weekly_channel_name = "weeklyNotif";
    public static String weekly_channel_desc = "a notification channel that gets used to send weekly notifications";
    public static String alarm_channel = "alarmChannel";
    public static String alarm_channel_desc = "a notification channel that gets used to send alarm notifications";

    private final long dailyInterval = 1000 * 60 * 60 * 24;
    private final long weeklyInterval = 1000 * 60 * 60 * 24 * 7;
    private final long monthlyInterval = 1000L * 60 * 60 * 24 * 7 * 4;
    private final long yearlyInterval = 1000L * 60 * 60 * 24 * 365;

    private static final String TAG = "NotificationSender";


    public NotificationSender(Context context) {
        this.context = context;
        manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    /**
     * Schedules a notification on the day 'event' takes place at 6am
     * @param event --> the event to schedule a notification for
     * @param job --> the job the event belongs to
     */
    public void scheduleDailyNotification(CalendarEvent event, String job) {
        String name = event.getName();
        String startTime = event.getBegin_time();
        String endTime = event.getEnd_time();
        int ID = event.getNotifID();

        // Information that will be needed in the notification
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("name", name);
        intent.putExtra("job", job);
        intent.putExtra("startTime", startTime);
        intent.putExtra("endTime", endTime);
        intent.putExtra("startDate", event.getBegin_date());
        intent.putExtra("endDate", event.getEnd_date());
        intent.putExtra("ID", ID);

        // Daily shift notifications will get sent at 6am on the day of the shift
        long milliStartDate = getMilliDateTime(event.getBegin_date(), 6,0);

        PendingIntent pendintent = PendingIntent.getBroadcast(context,
                ID,
                intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0);

        scheduleEvent(event,milliStartDate,pendintent);

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
        ZonedDateTime dateTime;
        if(hour < 0){
            hour = hour+24;
            dateTime = localDate.atTime(hour, minute).atZone(TimeZone.getDefault().toZoneId());
            return dateTime.toInstant().toEpochMilli() - dailyInterval;
        }
        dateTime = localDate.atTime(hour, minute).atZone(TimeZone.getDefault().toZoneId());
        return dateTime.toInstant().toEpochMilli();
    }

    /**
     * Finds the next Sunday from the current date
     * @return the Calender date of the next Sunday
     */
    private Calendar findNextSunday(){
        Calendar currTime = Calendar.getInstance();
        Calendar nextSunday = Calendar.getInstance();
        int weekDay = currTime.get(Calendar.DAY_OF_WEEK);
        int days = Calendar.SUNDAY - weekDay;
        int dayDiff = 0;

        if(days <= 0)
        {
            dayDiff = days+7;
        }
        nextSunday.set(Calendar.AM_PM, Calendar.AM);
        nextSunday.add(Calendar.DAY_OF_WEEK, dayDiff);
        nextSunday.set(Calendar.SECOND,0);
        nextSunday.set(Calendar.MINUTE, 0);
        nextSunday.set(Calendar.HOUR, 6);
        return nextSunday;
    }

    /**
     * Updates the weekly notification that gets sent every Sunday
     */
    public void updateWeeklyNotif(){
        Intent intent = new Intent(context, WeeklyNotificationReceiver.class);

        // Information that will be needed for the notification to send correct data
        long nextSunday = findNextSunday().getTimeInMillis();
        long weeklyInterval = 1000 * 60 * 60 * 24 * 7;
        intent.putExtra("nextSunday",nextSunday);
        intent.putExtra("interval", weeklyInterval);

        // The notification that will get sent
        PendingIntent pendintent = PendingIntent.getBroadcast(context,
                2,
                intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0);

        manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextSunday, pendintent);
    }

    /**
     * Cancels a notification if a CalenderEvent is deleted
     * @param event --> the event that was deleted
     */
    public void cancelNotification(CalendarEvent event){
        // Cancel notification associated with this event
        NotificationManager notifManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent notificationIntent = new Intent(context, NotificationReceiver.class);
        PendingIntent notificationPendingIntent = PendingIntent.getActivity(context,
                event.getNotifID(),
                notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if(notificationPendingIntent != null) {
            Log.d(TAG, "Canceled notification for: " + event.getName());
            notificationPendingIntent.cancel();
            notifManager.cancel(event.getNotifID());
        }
        else
            Log.e(TAG, "Error canceling notification for " + event.getName());

        // Cancel alarm associated with this event
        Intent alarmIntent = new Intent(context,AlarmReceiver.class);
        PendingIntent alarmPendingIntent = PendingIntent.getActivity(context,
                event.getAlarmID(),
                alarmIntent,
                PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if(alarmPendingIntent != null){
            Log.d(TAG, "Canceled alarm for: " + event.getName());
            alarmPendingIntent.cancel();
            notifManager.cancel(event.getAlarmID());
        }
        else
            Log.e(TAG, "Error canceling alarm for " + event.getName());
    }

    /**
     * Schedules a daily notification on startDate
     * @param pendIntent --> what should be scheduled
     * @param startDate --> the date to schedule the notification
     */
    private void scheduleOnce(PendingIntent pendIntent, long startDate){
        if(startDate >= System.currentTimeMillis()) {
            manager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    startDate,
                    pendIntent);
        }
    }

    /**
     * Schedules a repeating notification on startDate with interval 'interval'
     * @param pendIntent --> what should be scheduled
     * @param startDate --> the date to start the notification scheduling
     * @param interval --> time between notifications
     */
    private void scheduleRepeating(PendingIntent pendIntent, long startDate, long interval){
        manager.setRepeating(AlarmManager.RTC_WAKEUP,
                startDate,
                interval,
                pendIntent);
    }

    /**
     * Gets the integer hour and minute from a String time
     * @param time --> the String time to be converted
     * @return --> an array which has the hour in place 0 and the minute in place 1
     */
    private int[] getTime(String time){
        int hour, minute = 0;
        try{
            String[] times = time.split(":");
            hour = Integer.parseInt(times[0]);
            minute = Integer.parseInt(times[1]);
        } catch (NumberFormatException e){
            hour = 0;
        }
        return new int[]{hour, minute};
    }

    /**
     * Schedules an alarm for a shift if that shift has an alarm with it
     * @param event --> the shift that an alarm will be scheduled for
     * @param job --> the job that the shift belongs to
     */
    public void scheduleAlarm(CalendarEvent event, String job){
        if(event.getAlarmType() != AlarmType.NONE){
            Intent intent = new Intent(context, AlarmReceiver.class);
            int[] times = getTime(event.getBegin_time());
            int hour = times[0];
            int minute = times[1];

            // Extra information that will be needed when the alarm is sent
            String inHours = "";
            intent.putExtra("jobName", job);
            intent.putExtra("shiftName", event.getName());
            intent.putExtra("startTime", event.getBegin_time());
            intent.putExtra("endTime", event.getEnd_time());
            intent.putExtra("endDate", event.getEnd_date());
            intent.putExtra("alarmID",event.getAlarmID());

            // How early before the shift to schedule the alarm
            switch (event.getAlarmType()) {
                case ONE_HOUR: {
                    hour -= 1;
                    inHours = "one hour!";
                    break;
                }
                case TWO_HOUR: {
                    hour -= 2;
                    inHours = "two hours!";
                    break;
                }
                case THREE_HOUR: {
                    hour -= 3;
                    inHours = "three hours!";
                    break;
                }
            }
            intent.putExtra("inHours",inHours);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                    event.getAlarmID(),
                    intent,
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0);

            long startDate = getMilliDateTime(event.getBegin_date(),hour,minute);
            scheduleEvent(event, startDate, pendingIntent);
        }
    }

    /**
     * Schedules either and alarm or notification for 'event' at time 'startDate' using 'pendingIntent'
     * @param event --> the shift that is being scheduled for
     * @param startDate --> when to schedule the alarm/notification
     * @param pendingIntent --> what action is scheduled
     */
    private void scheduleEvent(CalendarEvent event, long startDate, PendingIntent pendingIntent){
        if (event.getRepeated() == RepeatType.NEVER) {
            scheduleOnce(pendingIntent, startDate);
        }
        else if (event.getRepeated() == RepeatType.DAILY) {
            scheduleRepeating(pendingIntent, startDate, dailyInterval);
        }
        else if(event.getRepeated() == RepeatType.WEEKLY){
            scheduleRepeating(pendingIntent, startDate, weeklyInterval);
        }
        else if(event.getRepeated() == RepeatType.MONTHLY){
            scheduleRepeating(pendingIntent, startDate, monthlyInterval);
        }
        else if(event.getRepeated() == RepeatType.ANNUALLY){
            scheduleRepeating(pendingIntent, startDate, yearlyInterval);
        }
    }
}