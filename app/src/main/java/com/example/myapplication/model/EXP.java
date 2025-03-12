package com.example.myapplication.model;

import java.io.Serializable;

public class EXP implements Serializable {
    private String description;
    private double amount;

    public EXP() {}

    public EXP(String description, double amount) {
        this.description = description;
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}