package com.example.myapplication.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

public class EXP implements Serializable {
    private String description;
    private double amount;
    private RepeatType repeatType = RepeatType.NEVER; // Default to one-off
    private String startDate;
    private String endDate;

    public EXP() {}

    public EXP(String description, double amount) {
        this.description = description;
        this.amount = amount;
        this.repeatType = RepeatType.NEVER;
    }

    public EXP(String description, double amount, RepeatType repeatType, String startDate, String endDate) {
        this.description = description;
        this.amount = amount;
        this.repeatType = repeatType;
        this.startDate = startDate;
        this.endDate = endDate;
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

    public RepeatType getRepeatType() {
        return repeatType;
    }

    public void setRepeatType(RepeatType repeatType) {
        this.repeatType = repeatType;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public List<Double> calculateExpenseDetails() {
        LocalDate start = LocalDate.parse(startDate);  // Assuming startDate is a String
        LocalDate end = LocalDate.parse(endDate);      // Assuming endDate is a String

        long days = ChronoUnit.DAYS.between(start, end) + 1;  // +1 to include both start and end dates
        long occurrences = 1;  // Default to one-time expense

        // Determine occurrences based on the repeat type
        switch (repeatType) {
            case DAILY:
                occurrences = days;
                break;
            case WEEKLY:
                occurrences = days / 7;
                break;
            case MONTHLY:
                occurrences = days / 30; // Approximate
                break;
            case ANNUALLY:
                occurrences = days / 365; // Approximate
                break;
            default:
                break;
        }

        if (occurrences < 1){
            occurrences = 1;}

        double totalExpense = amount * occurrences;
        double expenseRate = amount; // Average daily expense rate

        return Arrays.asList(expenseRate, totalExpense);
    }


}
