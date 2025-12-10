package com.togglecover.insurance.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CoverageStatusResponse {
    private String policyNumber;
    private Boolean isCoverageActiveToday;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate coverageDate;

    private String currentStatus;
    private Double premiumPaidToday;
    private Double walletBalance;
    private String gigPlatform;
    private String location;
}