package com.example.myapplication.model;

public class ExpenseModel {

    int id;
    String buy, reason;

    public ExpenseModel(int id, String buy, String reason) {
        this.id = id;
        this.buy = buy;
        this.reason = reason;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBuy() {
        return buy;
    }

    public void setBuy(String buy) {
        this.buy = buy;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }


}
