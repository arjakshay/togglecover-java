package com.togglecover.insurance.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CoverageResponse {
    private String policyNumber;
    private String status;
    private Boolean coverageActive;
    private String message;
    private Double premiumCharged;
    private Double coverageAmount;
    private String gigPlatform;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate coverageDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}