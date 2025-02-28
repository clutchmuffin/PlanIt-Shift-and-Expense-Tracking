package com.example.myapplication.model;

import java.util.Date;
// for monthly repeated events, which day should the event fall on

public class DayEvent {
    private String name;
    private Date begin_date, end_date;

    // how often is the DayEvent repeated?

    private RepeatType repeated;


    private MonthlyRepeatType repeated_monthly;

    // for repeated events, how long does it keep repeating

    private RepeatUntilType repeated_until;

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

        this.repeated = RepeatType.NEVER;
        this.repeated_monthly = MonthlyRepeatType.NEVER;
        this.repeated_until = RepeatUntilType.NEVER;
        this.repeated_until_date = end;
        this.repeated_reps = 0;
        this.repetition_step = 0;

    }

    public DayEvent(String n, Date begin, Date end, RepeatType repeat, MonthlyRepeatType monthly, RepeatUntilType until, Date until_date, int reps, int step) {
        this.name = n;
        this.begin_date = begin;
        this.end_date = end;

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

    public Date getBegin_date() {
        return begin_date;
    }

    public Date getEnd_date() {
        return end_date;
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

    public Date getRepeated_until_date() {
        return repeated_until_date;
    }

    public int getRepeated_reps() {
        return repeated_reps;
    }

    public int getRepetition_step() {
        return repetition_step;
    }
}
