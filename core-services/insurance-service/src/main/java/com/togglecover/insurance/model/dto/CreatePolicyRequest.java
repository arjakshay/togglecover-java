package com.togglecover.insurance.model.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatePolicyRequest {
    private Long userId;
    private String planCode;
    private Boolean autoRenew = true;
    private BigDecimal initialWalletTopUp;
}