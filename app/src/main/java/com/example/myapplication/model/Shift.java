package com.example.myapplication.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Shift implements Serializable {
    private LocalDate date;     // e.g., "2025-02-20"
    private LocalTime startTime; // e.g., "09:00 AM"
    private LocalTime endTime;   // e.g., "05:00 PM"

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");

    public Shift(LocalDate date, LocalTime startTime, LocalTime endTime) {
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Getters (and setters if needed)
    public String getDate() { return date.format(DATE_FORMATTER); }
    public String getStartTime() { return startTime.format(TIME_FORMATTER); }
    public String getEndTime() { return endTime.format(TIME_FORMATTER); }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public LocalDate getLocalDate() { return date; }
}
