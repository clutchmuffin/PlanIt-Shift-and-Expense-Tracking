package com.example.myapplication.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Job implements Serializable {
    private String title;
    private String subTitle;
    private String employer;
    private String location;
    private int color;
    private ArrayList<CalendarEvent> days;
    private ArrayList<EventSlot> shifts;

    public Job() { }

    public Job(String title, String subTitle, String employer, String location, int color) {
        this.title = title;
        this.subTitle = subTitle;
        this.employer = employer;
        this.location = location;
        this.color = color;
        this.shifts = new ArrayList<EventSlot>();
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String newTitle) {
        title = newTitle;
    }

    public String getSubTitle() {
        return subTitle;
    }
    public void setSubTitle(String newSubTitle) {
        subTitle = newSubTitle;
    }

    public String getEmployer() { return employer; }
    public void setEmployer(String newEmployer) { employer = newEmployer; }

    public String getLocation() { return location; }
    public void setLocation(String newLocation) { location = newLocation; }

    public int getColor() { return color; }
    public void setColor(int newColor) { color = newColor; }

    public ArrayList<EventSlot> getShifts() {
        return shifts;
    }
    public void addShift(EventSlot shift) {
        shifts.add(shift);
    }
}

