package com.example.myapplication.model;

import org.junit.Test;
import static org.junit.Assert.*;

public class ExpenseTest {

    @Test
    public void testDefaultConstructor() {
        Expense expense = new Expense();
        assertNull(expense.getDescription());
        assertEquals(0.0, expense.getAmount(), 0.001);
    }

    @Test
    public void testParameterizedConstructor() {
        Expense expense = new Expense("Groceries", 100.0);
        assertEquals("Groceries", expense.getDescription());
        assertEquals(100.0, expense.getAmount(), 0.001);
    }

    @Test
    public void testSettersAndGetters() {
        Expense expense = new Expense();

        expense.setDescription("Utilities");
        assertEquals("Utilities", expense.getDescription());

        expense.setAmount(150.0);
        assertEquals(150.0, expense.getAmount(), 0.001);
    }

    @Test
    public void testNegativeAmount() {
        Expense expense = new Expense();
        expense.setAmount(-50.0);
        assertEquals(-50.0, expense.getAmount(), 0.001);
    }

    @Test
    public void testEmptyDescription() {
        Expense expense = new Expense("", 0.0);
        assertEquals("", expense.getDescription());
    }
}