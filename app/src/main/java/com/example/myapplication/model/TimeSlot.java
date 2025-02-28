package com.example.myapplication.model;

import com.google.type.DateTime;

public class TimeSlot {
    private int event;

    private DateTime begin_local, end_local;

    private int begin_tz, end_tz;

    private boolean skipped;

    public TimeSlot() {}

    public TimeSlot(int eid, DateTime begin, DateTime end, int btz, int etz, boolean skip) {
        this.event = eid;
        this.begin_local = begin;
        this.end_local = end;
        this.begin_tz = btz;
        this.end_tz = etz;
        this.skipped = skip;
    }

    public int getEvent() {
        return event;
    }

    public DateTime getBegin_local() {
        return begin_local;
    }

    public DateTime getEnd_local() {
        return end_local;
    }

    public int getBegin_tz() {
        return begin_tz;
    }

    public int getEnd_tz() {
        return end_tz;
    }

    public boolean getSkipped() {
        return skipped;
    }
}
