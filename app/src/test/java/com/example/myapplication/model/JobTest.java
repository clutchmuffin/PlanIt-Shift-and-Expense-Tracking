package com.example.myapplication.model;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class JobTest {
    private Job job;
    private static final String TITLE = "Software Developer";
    private static final String SUBTITLE = "Junior";
    private static final String EMPLOYER = "Tech Corp";
    private static final String LOCATION = "New York";
    private static final int COLOR = 0xFF0000;

    @Before
    public void setUp() {
        job = new Job(TITLE, SUBTITLE, EMPLOYER, LOCATION, COLOR);
    }

    @Test
    public void testDefaultConstructor() {
        Job emptyJob = new Job();
        assertNotNull(emptyJob.getEvents());
        assertNotNull(emptyJob.getExpenses());
        assertTrue(emptyJob.getEvents().isEmpty());
        assertTrue(emptyJob.getExpenses().isEmpty());
    }

    @Test
    public void testParameterizedConstructor() {
        assertEquals(TITLE, job.getTitle());
        assertEquals(SUBTITLE, job.getSubTitle());
        assertEquals(EMPLOYER, job.getEmployer());
        assertEquals(LOCATION, job.getLocation());
        assertEquals(COLOR, job.getColor());
        assertEquals(0, job.getPayRate());
    }

    @Test
    public void testSettersAndGetters() {
        job.setTitle("New Title");
        assertEquals("New Title", job.getTitle());

        job.setSubTitle("Senior");
        assertEquals("Senior", job.getSubTitle());

        job.setEmployer("New Corp");
        assertEquals("New Corp", job.getEmployer());

        job.setLocation("Seattle");
        assertEquals("Seattle", job.getLocation());

        job.setColor(0x00FF00);
        assertEquals(0x00FF00, job.getColor());

        job.setPayRate(50);
        assertEquals(50, job.getPayRate());

        job.setUserId("user123");
        assertEquals("user123", job.getUserId());

        job.setJobId("job123");
        assertEquals("job123", job.getJobId());
    }

    @Test
    public void testAddEvent() {
        CalendarEvent event = new CalendarEvent();
        job.addEvent(event);
        assertEquals(1, job.getEvents().size());
        assertTrue(job.getEvents().contains(event));
    }

    @Test
    public void testAddExpense() {
        EXP expense = new EXP("Travel", 100.0);
        job.addExpense(expense);
        assertEquals(1, job.getExpenses().size());
        assertTrue(job.getExpenses().contains(expense));
    }

    @Test
    public void testMultipleEventsAndExpenses() {
        CalendarEvent event1 = new CalendarEvent();
        CalendarEvent event2 = new CalendarEvent();
        job.addEvent(event1);
        job.addEvent(event2);
        assertEquals(2, job.getEvents().size());

        EXP expense1 = new EXP("Food", 50.0);
        EXP expense2 = new EXP("Transport", 30.0);
        job.addExpense(expense1);
        job.addExpense(expense2);
        assertEquals(2, job.getExpenses().size());
    }
}