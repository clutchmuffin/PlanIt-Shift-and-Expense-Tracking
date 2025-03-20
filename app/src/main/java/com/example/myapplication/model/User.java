package com.example.myapplication.model;

import java.io.Serializable;

public class User implements Serializable {
    private String userId;
    private String name;
    private String username;
    private String email;

    public User() {
    }

    public User(String userId, String name, String username, String email) {
        this.userId = userId;
        this.name = name;
        this.username = username;
        this.email = email;
    }

    // Getters and setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}