package com.example.myapplication.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;



public class CalendarEvent {
    private String name;

    private int payRate;

    private int netPay;

    private String userId;

    private String begin_date;
    private String end_date;

    private String begin_time;
    private String end_time;

    private int begin_tz, end_tz;

    // how often is the DayEvent repeated?
    private RepeatType repeated;

    // if repeated monthly, is it numerical or weekday relative
    private MonthlyRepeatType repeated_monthly;

    // if repeated weekly, which days of the week?
    private ArrayList<String> repeated_dow;

    // for repeated events, how long does it keep repeating
    private RepeatUntilType repeated_until;

    // for events repeated until a certain date
    private String repeated_until_date;

    // for repeated events, step = every n weeks/months/years
    // for events repeated a certain number of times, how many reps?
    private int repetition_step, repeated_reps;
    private int notifID;
    private int color;

    public CalendarEvent() {}

    public CalendarEvent(String n, String uid, int payRate, String begin, String end, String begin_time, String end_time, RepeatType repeated, int ID) {
        this.name = n;
        this.userId = uid;
        this.payRate = payRate;
        this.begin_date = begin;
        this.end_date = end;
        this.begin_time = begin_time;
        this.end_time = end_time;
        this.repeated = repeated;
        this.netPay = calculatePay();

        this.begin_tz = 0;
        this.end_tz = 0;
        this.repeated_monthly = MonthlyRepeatType.NEVER;
        this.repeated_until = RepeatUntilType.NEVER;
        this.repeated_until_date = end;
        this.repeated_reps = 0;
        this.repetition_step = 0;

        this.notifID = ID;
    }

    public CalendarEvent(String n, String uid, String begin, String end, int b_tz, int e_tz, RepeatType repeat, MonthlyRepeatType monthly, RepeatUntilType until, String until_date, int reps, int step) {
        this.name = n;
        this.userId = uid;
        this.begin_date = begin;
        this.end_date = end;
        this.begin_tz = b_tz;
        this.end_tz = e_tz;

        this.repeated = repeat;
        this.repeated_monthly = monthly;
        this.repeated_until = until;
        this.repeated_until_date = until_date;
        this.repeated_reps = reps;
        this.repetition_step = step;

    }

    public String getName() {
        return name;
    }

    public String getUserId() {
        return userId;
    }

    public int getNotifID(){
        return notifID;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getBegin_date() {
        return begin_date;
    }

    public String getEnd_date() {
        return end_date;
    }

    public String getBegin_time() {
        return begin_time;
    }

    public String getEnd_time() {
        return end_time;
    }

    public int getPayRate() {
        return payRate;
    }
    public void setPayRate(int value) { this.payRate = value; }

    public int getNetPay() {
        return netPay;
    }
    public void setNetPay(int value) { this.netPay = value;}

    public RepeatType getRepeated() {
        return repeated;
    }

    public MonthlyRepeatType getRepeated_monthly() {
        return repeated_monthly;
    }

    public RepeatUntilType getRepeated_until() {
        return repeated_until;
    }

    public String getRepeated_until_date() {
        return repeated_until_date;
    }

    public int getRepeated_reps() {
        return repeated_reps;
    }

    public int getRepetition_step() {
        return repetition_step;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int calculatePay() {
        LocalDate startDate = LocalDate.parse(begin_date);
        LocalDate endDate = LocalDate.parse(end_date);
        LocalTime startTime = LocalTime.parse(begin_time);
        LocalTime endTime = LocalTime.parse(end_time);

        // Calculate days between dates (inclusive)
        long days = endDate.toEpochDay() - startDate.toEpochDay() + 1;

        // Calculate total duration in hours
        double totalHours;

        if (days == 1) {
            // Same day: just calculate hours between times
            double hours = (endTime.toSecondOfDay() - startTime.toSecondOfDay()) / 3600.0;
            totalHours = hours;
        } else {
            // Multiple days
            // First day: hours from start time to midnight
            double firstDayHours = (24 * 3600 - startTime.toSecondOfDay()) / 3600.0;
            // Last day: hours from midnight to end time
            double lastDayHours = endTime.toSecondOfDay() / 3600.0;
            // Middle days (if any): full 24 hours each
            double middleDaysHours = (days - 2) * 24;

            totalHours = firstDayHours + middleDaysHours + lastDayHours;
        }

        // Calculate pay (round to nearest integer)
        return (int) Math.round(totalHours * payRate);
    }
}
