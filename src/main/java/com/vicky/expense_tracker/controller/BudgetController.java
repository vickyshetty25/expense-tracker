package com.vicky.expense_tracker.controller;

import com.vicky.expense_tracker.dto.BudgetAlertResponse;
import com.vicky.expense_tracker.dto.BudgetRequest;
import com.vicky.expense_tracker.dto.MonthlyReportResponse;
import com.vicky.expense_tracker.model.Budget;
import com.vicky.expense_tracker.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PostMapping("/api/budgets")
    public ResponseEntity<Budget> createBudget(@Valid @RequestBody BudgetRequest request) {
        return ResponseEntity.ok(budgetService.createBudget(request));
    }

    @GetMapping("/api/budgets/alert")
    public ResponseEntity<List<BudgetAlertResponse>> getBudgetAlerts(
            @RequestParam Integer month,
            @RequestParam Integer year) {
        return ResponseEntity.ok(budgetService.getBudgetAlerts(month, year));
    }

    @GetMapping("/api/reports/monthly")
    public ResponseEntity<MonthlyReportResponse> getMonthlyReport(
            @RequestParam Integer month,
            @RequestParam Integer year) {
        return ResponseEntity.ok(budgetService.getMonthlyReport(month, year));
    }
}