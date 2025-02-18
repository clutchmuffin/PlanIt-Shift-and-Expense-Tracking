package com.example.myapplication.model;

import java.io.Serializable;

public class Shift implements Serializable {
    private String date;     // e.g., "2025-02-20"
    private String startTime; // e.g., "09:00 AM"
    private String endTime;   // e.g., "05:00 PM"

    public Shift(String date, String startTime, String endTime) {
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Getters (and setters if needed)
    public String getDate() { return date; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }

    public void setDate(String date) {
        this.date = date;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
