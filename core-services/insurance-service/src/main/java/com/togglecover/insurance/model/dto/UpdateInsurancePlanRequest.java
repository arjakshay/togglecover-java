package com.togglecover.insurance.model.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateInsurancePlanRequest {
    private String planName;
    private String description;
    private BigDecimal dailyPremium;
    private BigDecimal coverageAmount;
    private String coverageType;
    private Integer maxAge;
    private Integer minAge;
    private Integer waitingPeriodDays;
    private Boolean isActive;
}