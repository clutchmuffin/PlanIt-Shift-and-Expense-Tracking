package com.example.myapplication.model;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class JobSelectableTest {
    private JobSelectable jobSelectable;

    @Before
    public void setUp() {
        jobSelectable = new JobSelectable();
    }

    @Test
    public void testDefaultConstructor() {
        assertFalse(jobSelectable.isChecked());
        assertNotNull(jobSelectable.getEvents());
        assertNotNull(jobSelectable.getExpenses());
    }

    @Test
    public void testSetChecked() {
        jobSelectable.setChecked(true);
        assertTrue(jobSelectable.isChecked());

        jobSelectable.setChecked(false);
        assertFalse(jobSelectable.isChecked());
    }

    @Test
    public void testInheritedProperties() {
        String title = "Test Job";
        String subtitle = "Test Subtitle";
        String employer = "Test Employer";
        String location = "Test Location";
        int color = 0xFF0000;

        jobSelectable.setTitle(title);
        jobSelectable.setSubTitle(subtitle);
        jobSelectable.setEmployer(employer);
        jobSelectable.setLocation(location);
        jobSelectable.setColor(color);

        assertEquals(title, jobSelectable.getTitle());
        assertEquals(subtitle, jobSelectable.getSubTitle());
        assertEquals(employer, jobSelectable.getEmployer());
        assertEquals(location, jobSelectable.getLocation());
        assertEquals(color, jobSelectable.getColor());
    }
}