package com.example.myapplication.model;

import java.util.ArrayList;

public class SharedCal {
    private String name;
    private int members;
    private int colour;
    private ArrayList<Job> jobs;

    public SharedCal(String n, int m, int c, ArrayList<Job> j) {
        this.name = n;
        this.members = m;
        this.colour = c;
        this.jobs = j;
    }

    public String getName() {
        return name;
    }

    public int getMembers() {
        return members;
    }

    public int getColour() {
        return colour;
    }
    public ArrayList<Job> getJobs() {
        return jobs;
    }
}
