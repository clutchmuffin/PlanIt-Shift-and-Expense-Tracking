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
    private List<Shift> shifts;

    public Job(String title, String subTitle, String employer, String location, int color) {
        this.title = title;
        this.subTitle = subTitle;
        this.employer = employer;
        this.location = location;
        this.color = color;
        this.shifts = new ArrayList<>();
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

    public List<Shift> getShifts() {
        return shifts;
    }
    public void addShift(Shift shift) {
        shifts.add(shift);
    }
}

