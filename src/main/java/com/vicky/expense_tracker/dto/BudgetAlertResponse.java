package com.vicky.expense_tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class BudgetAlertResponse {
    private String categoryName;
    private BigDecimal budgetAmount;
    private BigDecimal spentAmount;
    private boolean overBudget;
    private BigDecimal difference;
}