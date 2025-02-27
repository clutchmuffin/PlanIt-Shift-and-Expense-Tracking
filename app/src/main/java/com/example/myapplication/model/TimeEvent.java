package com.example.myapplication.model;

import com.google.type.DateTime;

public class TimeEvent {
    private String name;

    private DateTime begin_local_time, end_local_time;

    public TimeEvent(){}

    public TimeEvent(String n, DateTime begin, DateTime end) {
        this.name = n;
        this.begin_local_time = begin;
        this.end_local_time = end;
    }

    public String getName() {
        return name;
    }

    public DateTime getBegin_local_time() {
        return begin_local_time;
    }

    public DateTime getEnd_local_time() {
        return end_local_time;
    }
}
