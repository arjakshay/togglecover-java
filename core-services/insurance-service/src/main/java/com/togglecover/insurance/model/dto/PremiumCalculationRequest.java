package com.togglecover.insurance.model.dto;

import lombok.Data;

@Data
public class PremiumCalculationRequest {
    private String planCode;
    private Double temperature;
    private String location;
    private String gigPlatform;
    private Integer noClaimYears;
    private Boolean isMonthlySubscription = false;
    private Boolean isAnnualSubscription = false;
}