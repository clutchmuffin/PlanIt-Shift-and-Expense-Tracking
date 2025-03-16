package com.example.myapplication.model;

public class SharedCard {
    private String name;
    private int members;
    private int colour;

    public SharedCard(String n, int m) {
        this.name = n;
        this.members = m;
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
}
