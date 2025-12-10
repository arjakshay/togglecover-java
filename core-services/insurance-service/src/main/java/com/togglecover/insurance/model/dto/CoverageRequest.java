package com.togglecover.insurance.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CoverageRequest {
    private String policyNumber;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate coverageDate;

    private Boolean toggleCoverage; // true to activate, false to deactivate
    private String location;
    private String gigPlatform;
    private Double temperature; // For dynamic pricing based on weather
}