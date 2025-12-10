package com.togglecover.insurance.controller;

import com.togglecover.insurance.model.dto.CoverageRequest;
import com.togglecover.insurance.model.dto.CoverageResponse;
import com.togglecover.insurance.model.dto.CoverageStatusResponse;
import com.togglecover.insurance.service.CoverageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/insurance/coverage")
@RequiredArgsConstructor
@Tag(name = "Coverage Management", description = "APIs for managing daily coverage toggling")
public class CoverageController {

    private final CoverageService coverageService;

    @PostMapping("/toggle")
    @Operation(summary = "Toggle daily coverage (activate/deactivate)")
    public ResponseEntity<CoverageResponse> toggleCoverage(
            @Valid @RequestBody CoverageRequest request) {
        CoverageResponse response = coverageService.toggleDailyCoverage(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{policyNumber}")
    @Operation(summary = "Get coverage status for a specific date")
    public ResponseEntity<CoverageStatusResponse> getCoverageStatus(
            @PathVariable String policyNumber,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        CoverageStatusResponse response = coverageService.getCoverageStatus(policyNumber, date);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/activate/{policyNumber}")
    @Operation(summary = "Activate coverage for today")
    public ResponseEntity<CoverageResponse> activateCoverage(
            @PathVariable String policyNumber,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String gigPlatform,
            @RequestParam(required = false) Double temperature) {

        CoverageRequest request = new CoverageRequest();
        request.setPolicyNumber(policyNumber);
        request.setToggleCoverage(true);
        request.setLocation(location);
        request.setGigPlatform(gigPlatform);
        request.setTemperature(temperature);
        request.setCoverageDate(LocalDate.now());

        CoverageResponse response = coverageService.toggleDailyCoverage(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/deactivate/{policyNumber}")
    @Operation(summary = "Deactivate coverage for today")
    public ResponseEntity<CoverageResponse> deactivateCoverage(@PathVariable String policyNumber) {
        CoverageRequest request = new CoverageRequest();
        request.setPolicyNumber(policyNumber);
        request.setToggleCoverage(false);
        request.setCoverageDate(LocalDate.now());

        CoverageResponse response = coverageService.toggleDailyCoverage(request);
        return ResponseEntity.ok(response);
    }
}