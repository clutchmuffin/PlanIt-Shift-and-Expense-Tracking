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
    private final Context context;
    public static String daily_channel_name = "dailyNotif";
    public static String daily_channel_desc = "a notification channel that gets used to send notifications for every shift";
    public static String weekly_channel_name = "weeklyNotif";
    public static String weekly_channel_desc = "a notification that gets used to send weekly notifications";

    public NotificationSender(Context context) {
        this.context = context;
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

        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // Schedules a notification to go off at 6am the day of the added shift if the shift isn't repeating
        if (event.getRepeated() == RepeatType.NEVER) {
            scheduleOnce(pendintent, milliStartDate, manager);
        }
        else if (event.getRepeated() == RepeatType.DAILY) {
            long dailyInterval = 1000 * 60 * 60 * 24;
            scheduleRepeating(pendintent, milliStartDate, dailyInterval, manager);
        }
        else if(event.getRepeated() == RepeatType.WEEKLY){
            long weeklyInterval = 1000 * 60 * 60 * 24 * 7;
            scheduleRepeating(pendintent, milliStartDate, weeklyInterval, manager);
        }
        else if(event.getRepeated() == RepeatType.MONTHLY){
            long monthlyInterval = 1000L * 60 * 60 * 24 * 7 * 4;
            scheduleRepeating(pendintent, milliStartDate, monthlyInterval, manager);
        }
        else if(event.getRepeated() == RepeatType.ANNUALLY){
            long yearlyInterval = 1000L * 60 * 60 * 24 * 365;
            scheduleRepeating(pendintent, milliStartDate, yearlyInterval, manager);
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

    /**
     * Updates the weekly notification that gets sent every Sunday
     */
    public void updateWeeklyNotif(){
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

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

        manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000, pendintent);
    }

    /**
     * Cancels a notification if a CalenderEvent is deleted
     * @param event --> the event that was deleted
     */
    public void cancelNotification(CalendarEvent event){
            Intent intent = new Intent(context, NotificationReceiver.class);
            PendingIntent pendintent = PendingIntent.getBroadcast(context,
                    event.getNotifID(),
                    intent,
                    PendingIntent.FLAG_IMMUTABLE);
            AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            manager.cancel(pendintent);
    }

    /**
     * Schedules a daily notification on startDate
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

    /**
     * Schedules a repeating notification on startDate with interval 'interval'
     * @param pendIntent --> what should be scheduled
     * @param startDate --> the date to start the notification scheduling
     * @param interval --> time between notifications
     * @param manager --> the alarm manager that schedules the notification
     */
    private void scheduleRepeating(PendingIntent pendIntent, long startDate, long interval, AlarmManager manager){
        manager.setRepeating(AlarmManager.RTC_WAKEUP,
                startDate,
                interval,
                pendIntent);
    }
}