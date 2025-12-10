package com.togglecover.insurance.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PolicyDTO {
    private Long id;
    private String policyNumber;
    private Long userId;
    private Long planId;
    private String planName;
    private String planCode;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private String status;
    private BigDecimal totalPremiumPaid;
    private Integer totalClaims;
    private BigDecimal totalClaimsAmount;
    private BigDecimal walletBalance;
    private Boolean autoRenew;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    private BigDecimal dailyPremium;
    private BigDecimal coverageAmount;
    private String coverageType;

    private Long daysRemaining;
}



