package com.vicky.expense_tracker.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class BudgetRequest {

    @NotNull
    private Long categoryId;

    @NotNull
    private Integer month;

    @NotNull
    private Integer year;

    @NotNull
    private BigDecimal amount;
}