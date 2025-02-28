package com.example.myapplication.model;

import java.util.Date;

public class DaySlot {
    private int event;

    private Date day;

    private boolean skipped;

    public DaySlot() {}

    public DaySlot(int eid, Date date, boolean skip) {
        this.event = eid;
        this.day = date;
        this.skipped = skip;
    }

    public int getEvent() {
        return event;
    }

    public Date getDay() {
        return day;
    }

    public boolean getSkipped() {
        return skipped;
    }
}
