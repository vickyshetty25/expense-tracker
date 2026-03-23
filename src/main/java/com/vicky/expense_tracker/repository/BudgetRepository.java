package com.vicky.expense_tracker.repository;

import com.vicky.expense_tracker.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findByUserIdAndMonthAndYear(Long userId, Integer month, Integer year);

    @Query("SELECT b FROM Budget b WHERE b.user.id = :userId " +
            "AND b.month = :month AND b.year = :year " +
            "AND b.category.id = :categoryId")
    java.util.Optional<Budget> findByUserAndCategoryAndMonthYear(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            @Param("month") Integer month,
            @Param("year") Integer year);
}