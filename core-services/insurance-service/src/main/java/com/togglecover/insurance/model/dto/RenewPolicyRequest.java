package com.togglecover.insurance.model.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RenewPolicyRequest {
    private String policyNumber;
    private Integer renewalMonths;
    private BigDecimal walletTopUp;
}