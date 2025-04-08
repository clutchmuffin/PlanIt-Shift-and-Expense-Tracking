package com.example.myapplication.model;

import org.junit.Test;
import static org.junit.Assert.*;

public class CalendarEventTest {

    @Test
    public void testConstructorAndGetters() {
        // Arrange
        String name = "Test Event";
        String userId = "user123";
        int payRate = 20;
        String beginDate = "2025-04-07";
        String endDate = "2025-04-07";
        String beginTime = "09:00:00";
        String endTime = "17:00:00";
        RepeatType repeat = RepeatType.NEVER;
        int notifId = 1;
        int alarmId = 2;
        AlarmType alarmType = AlarmType.ONE_HOUR;
        int jobColor = 0xFF0000; // Red

        // Act
        CalendarEvent event = new CalendarEvent(
                name, userId, payRate, beginDate, endDate,
                beginTime, endTime, repeat, notifId, alarmId,
                alarmType, jobColor
        );

        // Assert
        assertEquals(name, event.getName());
        assertEquals(userId, event.getUserId());
        assertEquals(payRate, event.getPayRate());
        assertEquals(beginDate, event.getBegin_date());
        assertEquals(endDate, event.getEnd_date());
        assertEquals(beginTime, event.getBegin_time());
        assertEquals(endTime, event.getEnd_time());
        assertEquals(repeat, event.getRepeated());
        assertEquals(notifId, event.getNotifID());
        assertEquals(alarmId, event.getAlarmID());
        assertEquals(alarmType, event.getAlarmType());
        assertEquals(jobColor, event.getJobColor());
    }

    @Test
    public void testCalculatePaySameDay() {
        // Arrange
        CalendarEvent event = new CalendarEvent(
                "Test Event", "user123", 20,
                "2025-04-07", "2025-04-07",
                "09:00:00", "17:00:00",
                RepeatType.NEVER, 1, 2,
                AlarmType.NONE, 0
        );

        // Act
        int calculatedPay = event.calculatePay();

        // Assert
        // 8 hours * $20/hour = $160
        assertEquals(160, calculatedPay);
    }

    @Test
    public void testCalculatePayMultipleDays() {
        // Arrange
        CalendarEvent event = new CalendarEvent(
                "Test Event", "user123", 20,
                "2025-04-07", "2025-04-08",
                "22:00:00", "06:00:00",
                RepeatType.NEVER, 1, 2,
                AlarmType.NONE, 0
        );

        // Act
        int calculatedPay = event.calculatePay();

        // Assert
        // First day: 2 hours (22:00-24:00)
        // Second day: 6 hours (00:00-06:00)
        // Total: 8 hours * $20/hour = $160
        assertEquals(160, calculatedPay);
    }

    @Test
    public void testSettersAndGetters() {
        // Arrange
        CalendarEvent event = new CalendarEvent();

        // Act
        event.setUserId("user123");
        event.setPayRate(25);
        event.setNetPay(200);
        event.setJobColor(0x00FF00);
        event.setBegin_tz(1);
        event.setEnd_tz(2);

        // Assert
        assertEquals("user123", event.getUserId());
        assertEquals(25, event.getPayRate());
        assertEquals(200, event.getNetPay());
        assertEquals(0x00FF00, event.getJobColor());
        assertEquals(1, event.getBegin_tz());
        assertEquals(2, event.getEnd_tz());
    }

    @Test
    public void testSecondaryConstructor() {
        // Arrange
        String name = "Test Event";
        String userId = "user123";
        String beginDate = "2025-04-07";
        String endDate = "2025-04-07";
        int beginTz = 1;
        int endTz = 2;
        RepeatType repeat = RepeatType.WEEKLY;
        MonthlyRepeatType monthly = MonthlyRepeatType.NEVER;
        RepeatUntilType until = RepeatUntilType.NEVER;
        String untilDate = "2025-05-07";
        int reps = 4;
        int step = 1;

        // Act
        CalendarEvent event = new CalendarEvent(
                name, userId, beginDate, endDate,
                beginTz, endTz, repeat, monthly,
                until, untilDate, reps, step
        );

        // Assert
        assertEquals(name, event.getName());
        assertEquals(userId, event.getUserId());
        assertEquals(beginDate, event.getBegin_date());
        assertEquals(endDate, event.getEnd_date());
        assertEquals(beginTz, event.getBegin_tz());
        assertEquals(endTz, event.getEnd_tz());
        assertEquals(repeat, event.getRepeated());
        assertEquals(monthly, event.getRepeated_monthly());
        assertEquals(until, event.getRepeated_until());
        assertEquals(untilDate, event.getRepeated_until_date());
        assertEquals(reps, event.getRepeated_reps());
        assertEquals(step, event.getRepetition_step());
    }
}