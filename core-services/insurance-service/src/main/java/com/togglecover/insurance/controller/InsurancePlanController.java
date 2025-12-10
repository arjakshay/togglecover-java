package com.togglecover.insurance.controller;


import com.togglecover.insurance.model.dto.CreateInsurancePlanRequest;
import com.togglecover.insurance.model.dto.InsurancePlanDTO;
import com.togglecover.insurance.model.dto.PlanComparisonDTO;
import com.togglecover.insurance.model.dto.UpdateInsurancePlanRequest;
import com.togglecover.insurance.service.InsurancePlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/insurance/plans")
@RequiredArgsConstructor
@Tag(name = "Insurance Plans", description = "APIs for managing insurance plans")
public class InsurancePlanController {

    private final InsurancePlanService insurancePlanService;

    @GetMapping
    @Operation(summary = "Get all active insurance plans")
    public ResponseEntity<List<InsurancePlanDTO>> getAllActivePlans() {
        List<InsurancePlanDTO> plans = insurancePlanService.getAllActivePlans();
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/{planCode}")
    @Operation(summary = "Get insurance plan by code")
    public ResponseEntity<InsurancePlanDTO> getPlanByCode(@PathVariable String planCode) {
        InsurancePlanDTO plan = insurancePlanService.getPlanByCode(planCode);
        return ResponseEntity.ok(plan);
    }

    @GetMapping("/coverage-type/{coverageType}")
    @Operation(summary = "Get plans by coverage type")
    public ResponseEntity<List<InsurancePlanDTO>> getPlansByCoverageType(
            @PathVariable String coverageType) {
        List<InsurancePlanDTO> plans = insurancePlanService.getPlansByCoverageType(coverageType);
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/eligible/{age}")
    @Operation(summary = "Get eligible plans by age")
    public ResponseEntity<List<InsurancePlanDTO>> getEligiblePlansByAge(
            @PathVariable Integer age) {
        List<InsurancePlanDTO> plans = insurancePlanService.getEligiblePlansByAge(age);
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/budget/{maxDailyPremium}")
    @Operation(summary = "Get plans within daily premium budget")
    public ResponseEntity<List<InsurancePlanDTO>> getPlansWithinBudget(
            @PathVariable Double maxDailyPremium) {
        List<InsurancePlanDTO> plans = insurancePlanService.getPlansWithinBudget(maxDailyPremium);
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/coverage/{minCoverage}")
    @Operation(summary = "Get plans with minimum coverage amount")
    public ResponseEntity<List<InsurancePlanDTO>> getPlansWithMinCoverage(
            @PathVariable Double minCoverage) {
        List<InsurancePlanDTO> plans = insurancePlanService.getPlansWithMinCoverage(minCoverage);
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/coverage-types")
    @Operation(summary = "Get all coverage types")
    public ResponseEntity<List<String>> getAllCoverageTypes() {
        List<String> coverageTypes = insurancePlanService.getAllCoverageTypes();
        return ResponseEntity.ok(coverageTypes);
    }

    @GetMapping("/popular")
    @Operation(summary = "Get most popular plan")
    public ResponseEntity<InsurancePlanDTO> getMostPopularPlan() {
        InsurancePlanDTO plan = insurancePlanService.getMostPopularPlan();
        return ResponseEntity.ok(plan);
    }

    @GetMapping("/{planCode}/premium/annual")
    @Operation(summary = "Calculate annual premium for a plan")
    public ResponseEntity<BigDecimal> calculateAnnualPremium(@PathVariable String planCode) {
        BigDecimal premium = insurancePlanService.calculateAnnualPremium(planCode);
        return ResponseEntity.ok(premium);
    }

    @GetMapping("/{planCode}/premium/monthly")
    @Operation(summary = "Calculate monthly premium for a plan")
    public ResponseEntity<BigDecimal> calculateMonthlyPremium(@PathVariable String planCode) {
        BigDecimal premium = insurancePlanService.calculateMonthlyPremium(planCode);
        return ResponseEntity.ok(premium);
    }

    @GetMapping("/compare")
    @Operation(summary = "Compare insurance plans")
    public ResponseEntity<List<PlanComparisonDTO>> comparePlans(
            @RequestParam(required = false) List<String> planCodes) {
        // Implementation for plan comparison
        // This would return a comparison of selected plans
        return ResponseEntity.ok(List.of());
    }

    @PostMapping
    @Operation(summary = "Create a new insurance plan (Admin only)")
    public ResponseEntity<InsurancePlanDTO> createPlan(
            @Valid @RequestBody CreateInsurancePlanRequest request) {
        InsurancePlanDTO plan = insurancePlanService.createPlan(convertToDTO(request));
        return ResponseEntity.ok(plan);
    }

    @PutMapping("/{planCode}")
    @Operation(summary = "Update an insurance plan (Admin only)")
    public ResponseEntity<InsurancePlanDTO> updatePlan(
            @PathVariable String planCode,
            @Valid @RequestBody UpdateInsurancePlanRequest request) {
        InsurancePlanDTO plan = insurancePlanService.updatePlan(planCode, convertToDTO(request));
        return ResponseEntity.ok(plan);
    }

    @DeleteMapping("/{planCode}")
    @Operation(summary = "Deactivate an insurance plan (Admin only)")
    public ResponseEntity<Void> deactivatePlan(@PathVariable String planCode) {
        insurancePlanService.deactivatePlan(planCode);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/health")
    @Operation(summary = "Health check for plans")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Insurance Plans API is running");
    }

    private InsurancePlanDTO convertToDTO(CreateInsurancePlanRequest request) {
        InsurancePlanDTO dto = new InsurancePlanDTO();
        dto.setPlanCode(request.getPlanCode());
        dto.setPlanName(request.getPlanName());
        dto.setDescription(request.getDescription());
        dto.setDailyPremium(request.getDailyPremium());
        dto.setCoverageAmount(request.getCoverageAmount());
        dto.setCoverageType(request.getCoverageType());
        dto.setMaxAge(request.getMaxAge());
        dto.setMinAge(request.getMinAge());
        dto.setWaitingPeriodDays(request.getWaitingPeriodDays());
        return dto;
    }

    private InsurancePlanDTO convertToDTO(UpdateInsurancePlanRequest request) {
        InsurancePlanDTO dto = new InsurancePlanDTO();
        dto.setPlanName(request.getPlanName());
        dto.setDescription(request.getDescription());
        dto.setDailyPremium(request.getDailyPremium());
        dto.setCoverageAmount(request.getCoverageAmount());
        dto.setCoverageType(request.getCoverageType());
        dto.setMaxAge(request.getMaxAge());
        dto.setMinAge(request.getMinAge());
        dto.setWaitingPeriodDays(request.getWaitingPeriodDays());
        dto.setIsActive(request.getIsActive());
        return dto;
    }
}