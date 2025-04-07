package com.example.myapplication.model;

import java.io.Serializable;
import java.util.ArrayList;

public class SharedCal implements Serializable {
    private String sharedId;
    private String name;
    private ArrayList<String> members;
    private ArrayList<CalendarEvent> events;

    public SharedCal() {}

    public SharedCal(String n, String id, String m, ArrayList<CalendarEvent> j) {
        this.name = n;
        this.members = new ArrayList<>();
        this.members.add(m);
        this.events = j;
        this.sharedId = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String n) {
        name = n;
    }

    public String getSharedId() {
        return sharedId;
    }
    public void setSharedId(String id) {
        sharedId = id;
    }

    public ArrayList<String> getMembers() {
        return members;
    }
    public void addMember(String id) {
        this.members.add(id);
    }


    public ArrayList<CalendarEvent> getEvents() {
        return events;
    }
}
