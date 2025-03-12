package com.example.myapplication.model;

public class Budget {

    private double budget;        // The budget set by the user for the category
    private double totalExpenses; // The total expenses for the category (initially 0)

    // Default constructor (required for Firestore)
    public Budget() {
        // Default values
        this.budget = 0.0;
        this.totalExpenses = 0.0;
    }

    // Constructor to initialize the budget and total expenses
    public Budget(double budget, double totalExpenses) {
        this.budget = budget;
        this.totalExpenses = totalExpenses;
    }

    // Getter and setter for budget
    public double getBudget() {
        return budget;
    }

    public void setBudget(double budget) {
        this.budget = budget;
    }

    // Getter and setter for totalExpenses
    public double getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(double totalExpenses) {
        this.totalExpenses = totalExpenses;
    }
}
