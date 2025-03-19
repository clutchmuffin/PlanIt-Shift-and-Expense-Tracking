package com.example.myapplication.model;

import java.io.Serializable;
import java.util.ArrayList;

public class Job implements Serializable {
    private int uid;
    private String title;
    private String subTitle;
    private String employer;
    private String location;
    private int color;
    private int pay_rate;
    private ArrayList<CalendarEvent> events;
    private ArrayList<EventSlot> eventSlots;
    private ArrayList<Expense> expense_list;

    public Job() {
        this.events = new ArrayList<>();
        this.expense_list = new ArrayList<>();
    }

    public Job(String title, String subTitle, String employer, String location, int color) {
        this.title = title;
        this.subTitle = subTitle;
        this.employer = employer;
        this.location = location;
        this.color = color;
        this.pay_rate = 0;
        this.events = new ArrayList<>();
        this.expense_list = new ArrayList<>();
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

    public ArrayList<CalendarEvent> getEvents() {
        return events;
    }
    public void addEvent(CalendarEvent e) {
        events.add(e);
    }

    public int getPayRate() { return this.pay_rate; }
    public void setPayRate(int pay) { pay_rate = pay; }

    public ArrayList<Expense> getExpenses() {
        return this.expense_list;
    }

    public void addExpense(Expense exp) {
        expense_list.add(exp);
    }
}

