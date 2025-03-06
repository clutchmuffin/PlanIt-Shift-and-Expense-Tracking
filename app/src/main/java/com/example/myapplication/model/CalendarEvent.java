package com.example.myapplication.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;



public class CalendarEvent {
    private String name;

    private int user_id;

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

    public CalendarEvent() {}

    public CalendarEvent(String n, int uid, String begin, String end, String begin_time, String end_time, RepeatType repeated) {
        this.name = n;
        this.user_id = uid;
        this.begin_date = begin;
        this.end_date = end;
        this.begin_time = begin_time;
        this.end_time = end_time;
        this.repeated = repeated;

        this.begin_tz = 0;
        this.end_tz = 0;
        this.repeated_monthly = MonthlyRepeatType.NEVER;
        this.repeated_until = RepeatUntilType.NEVER;
        this.repeated_until_date = end;
        this.repeated_reps = 0;
        this.repetition_step = 0;
    }

    public CalendarEvent(String n, int uid, String begin, String end, int b_tz, int e_tz, RepeatType repeat, MonthlyRepeatType monthly, RepeatUntilType until, String until_date, int reps, int step) {
        this.name = n;
        this.user_id = uid;
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

    public int getUser_id() {
        return user_id;
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
}
