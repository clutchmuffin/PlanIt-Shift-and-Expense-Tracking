package com.example.myapplication.model;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class UserTest {
    private User user;
    private String testUserId;
    private String testName;
    private String testUsername;
    private String testEmail;

    @Before
    public void setUp() {
        testUserId = "user123";
        testName = "John Doe";
        testUsername = "johndoe";
        testEmail = "john@example.com";
        user = new User(testUserId, testName, testUsername, testEmail);
    }

    @Test
    public void testDefaultConstructor() {
        User emptyUser = new User();
        assertNotNull(emptyUser);
    }

    @Test
    public void testParameterizedConstructor() {
        assertEquals(testUserId, user.getUserId());
        assertEquals(testName, user.getName());
        assertEquals(testUsername, user.getUsername());
        assertEquals(testEmail, user.getEmail());
    }

    @Test
    public void testSetAndGetUserId() {
        String newUserId = "user456";
        user.setUserId(newUserId);
        assertEquals(newUserId, user.getUserId());
    }

    @Test
    public void testSetAndGetName() {
        String newName = "Jane Doe";
        user.setName(newName);
        assertEquals(newName, user.getName());
    }

    @Test
    public void testSetAndGetUsername() {
        String newUsername = "janedoe";
        user.setUsername(newUsername);
        assertEquals(newUsername, user.getUsername());
    }

    @Test
    public void testSetAndGetEmail() {
        String newEmail = "jane@example.com";
        user.setEmail(newEmail);
        assertEquals(newEmail, user.getEmail());
    }
}