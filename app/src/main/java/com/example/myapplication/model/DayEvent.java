package com.example.myapplication.model;

import java.util.Date;

public class DayEvent {
    private String name;
    private Date begin_date, end_date;

    // how often is the DayEvent repeated?
    enum repeat_type {
        NEVER,
        DAILY,
        WEEKLY,
        MONTHLY,
        ANNUALLY
    }
    private repeat_type repeated;

    // for monthly repeated events, which day should the event fall on
    enum repeat_monthly_type {
        NEVER,
        SAME_DAY, // eg every 25th
        SAME_WEEKDAY // eg every second tuesday
    }
    private repeat_monthly_type repeated_monthly;

    // for repeated events, how long does it keep repeating
    enum repeated_until_type {
        NEVER,
        FOREVER,
        UNTIL_DATE,
        N_REPETITIONS
    }
    private repeated_until_type repeated_until;

    // for events repeated until a certain date
    private Date repeated_until_date;

    // for repeated events, step = every n weeks/months/years
    // for events repeated a certain number of times, how many reps?
    private int repetition_step, repeated_reps;

    public DayEvent() {}

    public DayEvent(String n, Date begin, Date end) {
        this.name = n;
        this.begin_date = begin;
        this.end_date = end;

        this.repeated = repeat_type.NEVER;
        this.repeated_monthly = repeat_monthly_type.NEVER;
        this.repeated_until = repeated_until_type.NEVER;
        this.repeated_until_date = end;
        this.repeated_reps = 0;
        this.repetition_step = 0;

    }

    public DayEvent(String n, Date begin, Date end, int repeat, int monthly, int until, Date until_date, int reps, int step) {
        this.name = n;
        this.begin_date = begin;
        this.end_date = end;

        this.repeated = repeat;

    }
}
