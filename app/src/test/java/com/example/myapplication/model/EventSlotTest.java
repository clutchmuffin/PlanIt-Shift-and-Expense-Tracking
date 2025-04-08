package com.example.myapplication.model;

import java.time.LocalDateTime;
import org.junit.Test;
import static org.junit.Assert.*;

public class EventSlotTest {

    @Test
    public void testDefaultConstructor() {
        // Act
        EventSlot slot = new EventSlot();

        // Assert
        assertNull(slot.getBegin_local());
        assertNull(slot.getEnd_local());
        assertEquals(0, slot.getEvent());
        assertEquals(0, slot.getBegin_tz());
        assertEquals(0, slot.getEnd_tz());
        assertFalse(slot.getSkipped());
    }

    @Test
    public void testParameterizedConstructorAndGetters() {
        // Arrange
        int eventId = 123;
        LocalDateTime beginTime = LocalDateTime.now();
        LocalDateTime endTime = beginTime.plusHours(1);
        int beginTz = 1;
        int endTz = 2;
        boolean skipped = true;

        // Act
        EventSlot slot = new EventSlot(eventId, beginTime, endTime, beginTz, endTz, skipped);

        // Assert
        assertEquals(eventId, slot.getEvent());
        assertEquals(beginTime, slot.getBegin_local());
        assertEquals(endTime, slot.getEnd_local());
        assertEquals(beginTz, slot.getBegin_tz());
        assertEquals(endTz, slot.getEnd_tz());
        assertTrue(slot.getSkipped());
    }

    @Test
    public void testNullDateTimes() {
        // Arrange
        int eventId = 123;
        LocalDateTime beginTime = null;
        LocalDateTime endTime = null;
        int beginTz = 1;
        int endTz = 2;
        boolean skipped = false;

        // Act
        EventSlot slot = new EventSlot(eventId, beginTime, endTime, beginTz, endTz, skipped);

        // Assert
        assertNull(slot.getBegin_local());
        assertNull(slot.getEnd_local());
        assertEquals(beginTz, slot.getBegin_tz());
        assertEquals(endTz, slot.getEnd_tz());
        assertFalse(slot.getSkipped());
    }
}