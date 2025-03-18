package com.example.myapplication.model;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.TimeZone;


public class NotificationSender {
    private Context context;
    public static String daily_channel_name = "dailyNotif";
    public static String daily_channel_desc = "a notification channel that gets used to send notifications for every shift";
    public static String weekly_channel_name = "weeklyNotif";
    public static String weekly_channel_desc = "a notification that gets used to send weekly notifications";

    public NotificationSender(Context context) {
        this.context = context;
    }

    public void scheduleDailyNotification(CalendarEvent event, String job) {
        String name = event.getName();
        String startTime = event.getBegin_time();
        String endTime = event.getEnd_time();
        int ID = event.getID();

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

        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // Schedules a notification to go off at 6am the day of the added shift if the shift isn't repeating
        if (event.getRepeated() == RepeatType.NEVER) {
            scheduleOnce(pendintent, milliStartDate, manager);
        }
        else if (event.getRepeated() != RepeatType.NEVER && event.getRepeated_until() == RepeatUntilType.NEVER) {
            scheduleOnce(pendintent, milliStartDate, manager);
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

    private Calendar findNextSunday(){
        Calendar currTime = Calendar.getInstance();
        Calendar nextSunday = Calendar.getInstance();
        nextSunday.set(Calendar.SECOND, 0);
        nextSunday.set(Calendar.MINUTE,0);
        nextSunday.set(Calendar.HOUR, 6);
        nextSunday.set(Calendar.AM_PM, Calendar.AM);
        nextSunday.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);

        if(!currTime.before(nextSunday)){
            int dayDiff = (7 + nextSunday.get(Calendar.DAY_OF_WEEK) - currTime.get(Calendar.DAY_OF_WEEK)) % 7;

            if(dayDiff == 0){
                dayDiff = 7;
            }
            nextSunday.add(Calendar.DAY_OF_MONTH, dayDiff);
        }
        System.out.println(nextSunday);
        return nextSunday;
    }

    private Calendar findLastSunday(){
        Calendar currTime = Calendar.getInstance();
        Calendar lastSunday = Calendar.getInstance();
        lastSunday.set(Calendar.SECOND, 0);
        lastSunday.set(Calendar.MINUTE,0);
        lastSunday.set(Calendar.HOUR, 6);
        lastSunday.set(Calendar.AM_PM, Calendar.AM);
        lastSunday.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);

        if(!currTime.after(lastSunday)){
            int dayDiff = (7 + currTime.get(Calendar.DAY_OF_WEEK) - lastSunday.get(Calendar.DAY_OF_WEEK)) % 7;

            if(dayDiff == 0){
                dayDiff = -7;
            }
            lastSunday.add(Calendar.DAY_OF_MONTH, dayDiff);
        }
        return lastSunday;
    }

    public void updateWeeklyNotif(){
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, WeeklyNotificationReceiver.class);

        long nextSunday = findNextSunday().getTimeInMillis();
        long weeklyInterval = 1000 * 60 * 60 * 24 * 7;
        intent.putExtra("nextSunday",nextSunday);
        intent.putExtra("interval", weeklyInterval);

        PendingIntent pendintent = PendingIntent.getBroadcast(context,
                2,
                intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0);

        manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextSunday, pendintent);
    }

    /**
     * Schedules a daily notification on startDate at hour:minute
     * @param pendIntent --> what should be scheduled
     * @param startDate --> the date to schedule the notification
     * @param manager --> the alarm manager that schedules the notification
     */
    private void scheduleOnce(PendingIntent pendIntent, long startDate, AlarmManager manager){
        manager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                startDate,
                pendIntent);
    }

    private void scheduleDaily(PendingIntent pendIntent, long startDate, long untilDate){

    }
}