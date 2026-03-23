package com.vicky.expense_tracker.service;

import com.vicky.expense_tracker.dto.BudgetAlertResponse;
import com.vicky.expense_tracker.dto.BudgetRequest;
import com.vicky.expense_tracker.dto.MonthlyReportResponse;
import com.vicky.expense_tracker.exception.ResourceNotFoundException;
import com.vicky.expense_tracker.model.Budget;
import com.vicky.expense_tracker.model.Category;
import com.vicky.expense_tracker.model.Expense;
import com.vicky.expense_tracker.model.User;
import com.vicky.expense_tracker.repository.BudgetRepository;
import com.vicky.expense_tracker.repository.CategoryRepository;
import com.vicky.expense_tracker.repository.ExpenseRepository;
import com.vicky.expense_tracker.repository.UserRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;

    public BudgetService(BudgetRepository budgetRepository,
                         CategoryRepository categoryRepository,
                         UserRepository userRepository,
                         ExpenseRepository expenseRepository) {
        this.budgetRepository = budgetRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.expenseRepository = expenseRepository;
    }

    private User getLoggedInUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public Budget createBudget(BudgetRequest request) {
        User user = getLoggedInUser();
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Budget budget = new Budget();
        budget.setUser(user);
        budget.setCategory(category);
        budget.setMonth(request.getMonth());
        budget.setYear(request.getYear());
        budget.setAmount(request.getAmount());

        return budgetRepository.save(budget);
    }

    public List<BudgetAlertResponse> getBudgetAlerts(Integer month, Integer year) {
        User user = getLoggedInUser();
        String cacheKey = user.getId() + "_" + month + "_" + year;
        return getBudgetAlertsFromCache(cacheKey, user.getId(), month, year);
    }

    @Cacheable(value = "budgetAlerts", key = "#cacheKey")
    public List<BudgetAlertResponse> getBudgetAlertsFromCache(String cacheKey,
                                                              Long userId,
                                                              Integer month,
                                                              Integer year) {
        List<Budget> budgets = budgetRepository.findByUserIdAndMonthAndYear(userId, month, year);
        List<BudgetAlertResponse> alerts = new ArrayList<>();

        for (Budget budget : budgets) {
            LocalDate start = LocalDate.of(year, month, 1);
            LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

            BigDecimal spent = expenseRepository.findByFilters(
                            userId, budget.getCategory().getId(),
                            start, end, PageRequest.of(0, Integer.MAX_VALUE))
                    .getContent()
                    .stream()
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            boolean overBudget = spent.compareTo(budget.getAmount()) > 0;
            BigDecimal difference = budget.getAmount().subtract(spent).abs();

            alerts.add(new BudgetAlertResponse(
                    budget.getCategory().getName(),
                    budget.getAmount(),
                    spent,
                    overBudget,
                    difference));
        }
        return alerts;
    }

    public MonthlyReportResponse getMonthlyReport(Integer month, Integer year) {
        User user = getLoggedInUser();
        String cacheKey = user.getId() + "_" + month + "_" + year;
        return getMonthlyReportFromCache(cacheKey, user.getId(), month, year);
    }

    @Cacheable(value = "reports", key = "#cacheKey")
    public MonthlyReportResponse getMonthlyReportFromCache(String cacheKey,
                                                           Long userId,
                                                           Integer month,
                                                           Integer year) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        List<Expense> expenses = expenseRepository.findByFilters(
                userId, null, start, end,
                PageRequest.of(0, Integer.MAX_VALUE)).getContent();

        BigDecimal total = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> byCategory = expenses.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getCategory() != null
                                ? e.getCategory().getName() : "Uncategorized",
                        Collectors.reducing(BigDecimal.ZERO,
                                Expense::getAmount, BigDecimal::add)));

        return new MonthlyReportResponse(month, year, total, byCategory);
    }
}