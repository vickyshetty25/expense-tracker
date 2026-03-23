package com.vicky.expense_tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

@Data
@AllArgsConstructor
public class MonthlyReportResponse implements Serializable {
    private Integer month;
    private Integer year;
    private BigDecimal totalSpent;
    private Map<String, BigDecimal> spentByCategory;
}