package com.example.myapplication.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class BudgetTest {

    private static final double DELTA = 0.001; // Delta for double comparisons

    @Test
    public void testDefaultConstructor() {
        Budget budget = new Budget();
        assertEquals(0.0, budget.getBudget(), DELTA);
        assertEquals(0.0, budget.getTotalExpenses(), DELTA);
    }

    @Test
    public void testParameterizedConstructor() {
        double budgetValue = 100.0;
        double expensesValue = 50.0;
        Budget budget = new Budget(budgetValue, expensesValue);
        assertEquals(budgetValue, budget.getBudget(), DELTA);
        assertEquals(expensesValue, budget.getTotalExpenses(), DELTA);
    }

    @Test
    public void testGetSetBudget() {
        Budget budget = new Budget();

        // Set and verify a positive value
        budget.setBudget(500.0);
        assertEquals(500.0, budget.getBudget(), DELTA);

        // Update the value and verify again
        budget.setBudget(750.0);
        assertEquals(750.0, budget.getBudget(), DELTA);
    }

    @Test
    public void testGetSetTotalExpenses() {
        Budget budget = new Budget();

        // Set and verify a positive value
        budget.setTotalExpenses(250.0);
        assertEquals(250.0, budget.getTotalExpenses(), DELTA);

        // Update the value and verify again
        budget.setTotalExpenses(300.0);
        assertEquals(300.0, budget.getTotalExpenses(), DELTA);
    }

    @Test
    public void testNegativeValues() {
        // Test via constructor
        Budget budget1 = new Budget(-100.0, -50.0);
        assertEquals(-100.0, budget1.getBudget(), DELTA);
        assertEquals(-50.0, budget1.getTotalExpenses(), DELTA);

        // Test via setters
        Budget budget2 = new Budget();
        budget2.setBudget(-200.0);
        budget2.setTotalExpenses(-75.0);
        assertEquals(-200.0, budget2.getBudget(), DELTA);
        assertEquals(-75.0, budget2.getTotalExpenses(), DELTA);
    }

    @Test
    public void testLargeValues() {
        double largeValue = 1_000_000.0;
        Budget budget = new Budget(largeValue, largeValue);
        assertEquals(largeValue, budget.getBudget(), DELTA);
        assertEquals(largeValue, budget.getTotalExpenses(), DELTA);
    }
}