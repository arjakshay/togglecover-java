package com.togglecover.insurance.model.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PlanComparisonDTO {
    private String planCode;
    private String planName;
    private BigDecimal dailyPremium;
    private BigDecimal monthlyPremium;
    private BigDecimal annualPremium;
    private BigDecimal coverageAmount;
    private String coverageType;
    private String bestFor;
    private Double valueScore; // Calculated score based on premium vs coverage
}