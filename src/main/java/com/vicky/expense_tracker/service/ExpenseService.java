package com.vicky.expense_tracker.service;

import com.vicky.expense_tracker.dto.ExpenseRequest;
import com.vicky.expense_tracker.exception.ForbiddenException;
import com.vicky.expense_tracker.exception.ResourceNotFoundException;
import com.vicky.expense_tracker.model.Category;
import com.vicky.expense_tracker.model.Expense;
import com.vicky.expense_tracker.model.User;
import com.vicky.expense_tracker.repository.CategoryRepository;
import com.vicky.expense_tracker.repository.ExpenseRepository;
import com.vicky.expense_tracker.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.time.LocalDate;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public ExpenseService(ExpenseRepository expenseRepository,
                          CategoryRepository categoryRepository,
                          UserRepository userRepository) {
        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    private User getLoggedInUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public Expense createExpense(ExpenseRequest request) {
        User user = getLoggedInUser();

        Expense expense = new Expense();
        expense.setTitle(request.getTitle());
        expense.setAmount(request.getAmount());
        expense.setDate(request.getDate());
        expense.setNote(request.getNote());
        expense.setUser(user);

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            expense.setCategory(category);
        }

        return expenseRepository.save(expense);
    }

    public Page<Expense> getExpenses(Long categoryId, LocalDate startDate,
                                     LocalDate endDate, int page, int size) {
        User user = getLoggedInUser();
        return expenseRepository.findByFilters(
                user.getId(), categoryId, startDate, endDate,
                PageRequest.of(page, size));
    }

    public Expense getExpenseById(Long id) {
        User user = getLoggedInUser();
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
        if (!expense.getUser().getId().equals(user.getId()))
            throw new ForbiddenException("You do not own this resource");
        return expense;
    }

    public Expense updateExpense(Long id, ExpenseRequest request) {
        Expense expense = getExpenseById(id);

        expense.setTitle(request.getTitle());
        expense.setAmount(request.getAmount());
        expense.setDate(request.getDate());
        expense.setNote(request.getNote());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            expense.setCategory(category);
        }

        return expenseRepository.save(expense);
    }

    public void deleteExpense(Long id) {
        Expense expense = getExpenseById(id);
        expenseRepository.delete(expense);
    }
}