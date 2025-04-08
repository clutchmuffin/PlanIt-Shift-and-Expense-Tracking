package com.example.myapplication.model;

import org.junit.Test;
import static org.junit.Assert.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EXPTest {

    @Test
    public void testDefaultConstructor() {
        EXP exp = new EXP();
        assertNull(exp.getDescription());
        assertEquals(0.0, exp.getAmount(), 0.001);
        assertEquals(RepeatType.NEVER, exp.getRepeatType());
    }

    @Test
    public void testBasicConstructor() {
        EXP exp = new EXP("Groceries", 100.0);
        assertEquals("Groceries", exp.getDescription());
        assertEquals(100.0, exp.getAmount(), 0.001);
        assertEquals(RepeatType.NEVER, exp.getRepeatType());

        // Check if dateCreated is today
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        assertEquals(today, exp.getDateCreated());
    }

    @Test
    public void testFullConstructor() {
        EXP exp = new EXP("Rent", 1000.0, RepeatType.MONTHLY, "2024-01-01", "2024-12-31");
        assertEquals("Rent", exp.getDescription());
        assertEquals(1000.0, exp.getAmount(), 0.001);
        assertEquals(RepeatType.MONTHLY, exp.getRepeatType());
        assertEquals("2024-01-01", exp.getStartDate());
        assertEquals("2024-12-31", exp.getEndDate());
    }

    @Test
    public void testSettersAndGetters() {
        EXP exp = new EXP();

        exp.setDescription("Utilities");
        assertEquals("Utilities", exp.getDescription());

        exp.setAmount(150.0);
        assertEquals(150.0, exp.getAmount(), 0.001);

        exp.setRepeatType(RepeatType.WEEKLY);
        assertEquals(RepeatType.WEEKLY, exp.getRepeatType());

        exp.setStartDate("2024-03-01");
        assertEquals("2024-03-01", exp.getStartDate());

        exp.setEndDate("2024-03-31");
        assertEquals("2024-03-31", exp.getEndDate());

        exp.setDateCreated("2024-03-01");
        assertEquals("2024-03-01", exp.getDateCreated());
    }

    @Test
    public void testCalculateExpenseDetailsOneTime() {
        EXP exp = new EXP("One-time purchase", 100.0, RepeatType.NEVER, "2024-03-01", "2024-03-01");
        List<Double> details = exp.calculateExpenseDetails();

        assertEquals(100.0, details.get(0), 0.001); // Daily rate
        assertEquals(100.0, details.get(1), 0.001); // Total expense
    }

    @Test
    public void testCalculateExpenseDetailsDaily() {
        EXP exp = new EXP("Daily expense", 10.0, RepeatType.DAILY, "2024-03-01", "2024-03-07");
        List<Double> details = exp.calculateExpenseDetails();

        assertEquals(10.0, details.get(0), 0.001);  // Daily rate
        assertEquals(70.0, details.get(1), 0.001);  // Total expense (7 days * $10)
    }

    @Test
    public void testCalculateExpenseDetailsWeekly() {
        EXP exp = new EXP("Weekly expense", 70.0, RepeatType.WEEKLY, "2024-03-01", "2024-03-28");
        List<Double> details = exp.calculateExpenseDetails();

        assertEquals(70.0, details.get(0), 0.001);    // Weekly rate
        assertEquals(280.0, details.get(1), 0.001);   // Total expense (4 weeks * $70)
    }

    @Test
    public void testCalculateExpenseDetailsMonthly() {
        EXP exp = new EXP("Monthly expense", 100.0, RepeatType.MONTHLY, "2024-01-01", "2024-03-31");
        List<Double> details = exp.calculateExpenseDetails();

        assertEquals(100.0, details.get(0), 0.001);   // Monthly rate
        assertEquals(300.0, details.get(1), 0.001);   // Total expense (3 months * $100)
    }

    @Test
    public void testCalculateExpenseDetailsAnnually() {
        EXP exp = new EXP("Annual expense", 1200.0, RepeatType.ANNUALLY, "2024-01-01", "2024-12-31");
        List<Double> details = exp.calculateExpenseDetails();

        assertEquals(1200.0, details.get(0), 0.001);  // Annual rate
        assertEquals(1200.0, details.get(1), 0.001);  // Total expense (1 year * $1200)
    }
}