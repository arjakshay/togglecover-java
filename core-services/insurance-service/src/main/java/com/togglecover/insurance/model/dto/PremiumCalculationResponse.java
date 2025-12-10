package com.togglecover.insurance.model.dto;

import lombok.Data;

@Data
public class PremiumCalculationResponse {
    private String planCode;
    private String planName;
    private Double basePremium;
    private Double calculatedPremium;
    private Double weatherMultiplier;
    private Double locationMultiplier;
    private Double platformMultiplier;
    private Double timeMultiplier;
    private Double noClaimDiscount;
    private Double finalPremium;
    private String currency = "INR";
}