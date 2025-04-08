package com.example.myapplication.model;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;

public class SharedCalTest {
    private SharedCal sharedCal;
    private String testName;
    private String testId;
    private String testCode;
    private String testMember;
    private ArrayList<CalendarEvent> testEvents;

    @Before
    public void setUp() {
        testName = "Test Calendar";
        testId = "cal123";
        testCode = "ABC123";
        testMember = "user123";
        testEvents = new ArrayList<>();
        sharedCal = new SharedCal(testName, testId, testCode, testMember, testEvents);
    }

    @Test
    public void testDefaultConstructor() {
        SharedCal cal = new SharedCal();
        assertNotNull(cal);
    }

    @Test
    public void testParameterizedConstructor() {
        assertEquals(testName, sharedCal.getName());
        assertEquals(testId, sharedCal.getSharedId());
        assertEquals(testCode, sharedCal.getCode());
        assertEquals(testEvents, sharedCal.getEvents());
        assertEquals(1, sharedCal.getMembers().size());
        assertTrue(sharedCal.getMembers().contains(testMember));
    }

    @Test
    public void testSetAndGetName() {
        String newName = "New Calendar";
        sharedCal.setName(newName);
        assertEquals(newName, sharedCal.getName());
    }

    @Test
    public void testSetAndGetSharedId() {
        String newId = "newId123";
        sharedCal.setSharedId(newId);
        assertEquals(newId, sharedCal.getSharedId());
    }

    @Test
    public void testGetCode() {
        assertEquals(testCode, sharedCal.getCode());
    }

    @Test
    public void testAddMember() {
        String newMember = "user456";
        sharedCal.addMember(newMember);
        assertTrue(sharedCal.getMembers().contains(newMember));
        assertEquals(2, sharedCal.getMembers().size());
    }

    @Test
    public void testGetEvents() {
        ArrayList<CalendarEvent> events = sharedCal.getEvents();
        assertNotNull(events);
        assertEquals(testEvents, events);
    }
}