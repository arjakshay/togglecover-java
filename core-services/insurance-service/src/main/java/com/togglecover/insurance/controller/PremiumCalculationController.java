package com.togglecover.insurance.controller;

import com.togglecover.insurance.model.dto.PremiumCalculationRequest;
import com.togglecover.insurance.model.dto.PremiumCalculationResponse;
import com.togglecover.insurance.service.InsurancePlanService;
import com.togglecover.insurance.service.PremiumCalculatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/insurance/premium")
@RequiredArgsConstructor
@Tag(name = "Premium Calculation", description = "APIs for calculating insurance premiums")
public class PremiumCalculationController {

    private final PremiumCalculatorService premiumCalculatorService;
    private final InsurancePlanService insurancePlanService;

    @PostMapping("/calculate")
    @Operation(summary = "Calculate premium with dynamic factors")
    public ResponseEntity<PremiumCalculationResponse> calculatePremium(
            @RequestBody PremiumCalculationRequest request) {

        // Get base premium from plan
        var plan = insurancePlanService.getPlanByCode(request.getPlanCode());
        BigDecimal basePremium = plan.getDailyPremium();

        // Calculate various multipliers
        BigDecimal weatherMultiplier = premiumCalculatorService.calculateWeatherRiskMultiplier(
                request.getTemperature(), request.getLocation());

        BigDecimal locationMultiplier = premiumCalculatorService.calculateLocationRiskMultiplier(
                request.getLocation());

        BigDecimal platformMultiplier = premiumCalculatorService.calculateGigPlatformRiskMultiplier(
                request.getGigPlatform());

        BigDecimal timeMultiplier = premiumCalculatorService.calculateTimeOfDayMultiplier();

        BigDecimal noClaimDiscount = premiumCalculatorService.calculateNoClaimBonus(
                request.getNoClaimYears());

        // Calculate final premium
        BigDecimal calculatedPremium = premiumCalculatorService.calculateDailyPremium(
                basePremium,
                request.getTemperature(),
                request.getLocation(),
                request.getGigPlatform()
        );

        // Apply subscription discounts
        BigDecimal finalPremium = calculatedPremium;
        if (Boolean.TRUE.equals(request.getIsMonthlySubscription())) {
            finalPremium = premiumCalculatorService.calculateMonthlyPremium(calculatedPremium);
        } else if (Boolean.TRUE.equals(request.getIsAnnualSubscription())) {
            finalPremium = premiumCalculatorService.calculateAnnualPremium(calculatedPremium);
        }

        // Create response
        PremiumCalculationResponse response = new PremiumCalculationResponse();
        response.setPlanCode(request.getPlanCode());
        response.setPlanName(plan.getPlanName());
        response.setBasePremium(basePremium.doubleValue());
        response.setCalculatedPremium(calculatedPremium.doubleValue());
        response.setWeatherMultiplier(weatherMultiplier.doubleValue());
        response.setLocationMultiplier(locationMultiplier.doubleValue());
        response.setPlatformMultiplier(platformMultiplier.doubleValue());
        response.setTimeMultiplier(timeMultiplier.doubleValue());
        response.setNoClaimDiscount(noClaimDiscount.doubleValue());
        response.setFinalPremium(finalPremium.doubleValue());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    @Operation(summary = "Health check")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Premium Calculation Service is running");
    }
}