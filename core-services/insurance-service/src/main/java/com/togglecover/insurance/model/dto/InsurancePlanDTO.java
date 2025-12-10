package com.togglecover.insurance.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InsurancePlanDTO {
    private Long id;
    private String planCode;
    private String planName;
    private String description;
    private BigDecimal dailyPremium;
    private BigDecimal coverageAmount;
    private String coverageType;
    private Integer maxAge;
    private Integer minAge;
    private Integer waitingPeriodDays;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Calculated fields
    private BigDecimal monthlyPremium;
    private BigDecimal annualPremium;
}