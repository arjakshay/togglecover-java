package com.togglecover.insurance.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class PremiumCalculatorService {

    @Value("${insurance.daily-premium-rate:5.0}")
    private Double baseDailyPremium;

    @Value("${insurance.weather-risk.threshold-temperature:35}")
    private Double weatherThresholdTemperature;

    @Value("${insurance.weather-risk.risk-multiplier:1.5}")
    private Double weatherRiskMultiplier;

    @Value("${insurance.weather-risk.high-risk-zones:Mumbai,Chennai,Delhi,Bangalore}")
    private List<String> highRiskZones;

    private final List<String> nightTimeGigPlatforms = Arrays.asList("SWIGGY", "ZOMATO");
    private final List<String> highRiskGigPlatforms = Arrays.asList("ZEPTO", "INSTAMART");

    /**
     * Calculate daily premium based on various factors
     */
    public BigDecimal calculateDailyPremium(BigDecimal basePremium,
                                            Double temperature,
                                            String location,
                                            String gigPlatform) {

        BigDecimal calculatedPremium = basePremium;

        // Apply weather risk multiplier
        if (temperature != null) {
            calculatedPremium = calculatedPremium.multiply(
                    calculateWeatherRiskMultiplier(temperature, location)
            );
        }

        // Apply location risk multiplier
        calculatedPremium = calculatedPremium.multiply(
                calculateLocationRiskMultiplier(location)
        );

        // Apply gig platform risk multiplier
        calculatedPremium = calculatedPremium.multiply(
                calculateGigPlatformRiskMultiplier(gigPlatform)
        );

        // Apply time of day multiplier
        calculatedPremium = calculatedPremium.multiply(
                calculateTimeOfDayMultiplier()
        );

        // Round to 2 decimal places
        calculatedPremium = calculatedPremium.setScale(2, RoundingMode.HALF_UP);

        log.debug("Premium calculated: Base={}, Final={}, Factors: temp={}, location={}, platform={}",
                basePremium, calculatedPremium, temperature, location, gigPlatform);

        return calculatedPremium;
    }

    /**
     * Calculate weather risk multiplier based on temperature and location
     */
    public BigDecimal calculateWeatherRiskMultiplier(Double temperature, String location) {
        BigDecimal multiplier = BigDecimal.ONE;

        if (temperature != null) {
            // Higher temperature = higher risk
            if (temperature > weatherThresholdTemperature) {
                // For every 5 degrees above threshold, increase multiplier by 0.1
                double degreesAbove = temperature - weatherThresholdTemperature;
                double additionalRisk = Math.floor(degreesAbove / 5) * 0.1;
                multiplier = multiplier.add(BigDecimal.valueOf(additionalRisk));

                // Cap at weatherRiskMultiplier
                if (multiplier.compareTo(BigDecimal.valueOf(weatherRiskMultiplier)) > 0) {
                    multiplier = BigDecimal.valueOf(weatherRiskMultiplier);
                }
            }

            // Extreme cold also increases risk
            if (temperature < 10) {
                multiplier = multiplier.multiply(BigDecimal.valueOf(1.2));
            }
        }

        // Monsoon/rainy season check (simplified)
        if (location != null && isMonsoonSeason() &&
                (location.toLowerCase().contains("mumbai") ||
                        location.toLowerCase().contains("chennai") ||
                        location.toLowerCase().contains("kolkata"))) {
            multiplier = multiplier.multiply(BigDecimal.valueOf(1.3));
        }

        return multiplier;
    }

    /**
     * Calculate location risk multiplier
     */
    public BigDecimal calculateLocationRiskMultiplier(String location) {
        if (location == null || location.isEmpty()) {
            return BigDecimal.ONE;
        }

        String locationLower = location.toLowerCase();

        // Check for high-risk zones
        for (String highRiskZone : highRiskZones) {
            if (locationLower.contains(highRiskZone.toLowerCase())) {
                return BigDecimal.valueOf(1.4); // 40% higher for high-risk zones
            }
        }

        // Check for specific high-risk areas
        if (locationLower.contains("industrial") ||
                locationLower.contains("construction") ||
                locationLower.contains("highway")) {
            return BigDecimal.valueOf(1.5);
        }

        // Residential areas have lower risk
        if (locationLower.contains("residential") ||
                locationLower.contains("society") ||
                locationLower.contains("colony")) {
            return BigDecimal.valueOf(0.9); // 10% discount
        }

        return BigDecimal.ONE;
    }

    /**
     * Calculate gig platform risk multiplier
     */
    public BigDecimal calculateGigPlatformRiskMultiplier(String gigPlatform) {
        if (gigPlatform == null || gigPlatform.isEmpty()) {
            return BigDecimal.ONE;
        }

        // High-risk platforms (quick delivery, more accidents)
        if (highRiskGigPlatforms.contains(gigPlatform.toUpperCase())) {
            return BigDecimal.valueOf(1.3); // 30% higher
        }

        // Night-time delivery platforms
        if (nightTimeGigPlatforms.contains(gigPlatform.toUpperCase())) {
            LocalTime now = LocalTime.now();
            // Check if it's night time (8 PM to 6 AM)
            if (now.isAfter(LocalTime.of(20, 0)) || now.isBefore(LocalTime.of(6, 0))) {
                return BigDecimal.valueOf(1.4); // 40% higher at night
            }
        }

        return BigDecimal.ONE;
    }

    /**
     * Calculate time of day multiplier
     */
    public BigDecimal calculateTimeOfDayMultiplier() {
        LocalTime now = LocalTime.now();

        // Peak hours (5 PM to 9 PM) - higher risk
        if (now.isAfter(LocalTime.of(17, 0)) && now.isBefore(LocalTime.of(21, 0))) {
            return BigDecimal.valueOf(1.2);
        }

        // Night hours (10 PM to 5 AM) - highest risk
        if (now.isAfter(LocalTime.of(22, 0)) || now.isBefore(LocalTime.of(5, 0))) {
            return BigDecimal.valueOf(1.5);
        }

        // Midday (11 AM to 3 PM) - lower risk
        if (now.isAfter(LocalTime.of(11, 0)) && now.isBefore(LocalTime.of(15, 0))) {
            return BigDecimal.valueOf(0.9);
        }

        return BigDecimal.ONE;
    }

    /**
     * Calculate monthly premium (for subscription plans)
     */
    public BigDecimal calculateMonthlyPremium(BigDecimal dailyPremium) {
        // 30 days * daily premium, with 10% discount for monthly subscription
        return dailyPremium.multiply(BigDecimal.valueOf(30))
                .multiply(BigDecimal.valueOf(0.9))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate annual premium (for yearly plans)
     */
    public BigDecimal calculateAnnualPremium(BigDecimal dailyPremium) {
        // 365 days * daily premium, with 20% discount for annual subscription
        return dailyPremium.multiply(BigDecimal.valueOf(365))
                .multiply(BigDecimal.valueOf(0.8))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate refund amount for early deactivation
     */
    public BigDecimal calculateRefundAmount(BigDecimal premiumPaid,
                                            LocalTime startTime,
                                            LocalTime endTime) {
        if (startTime == null || endTime == null) {
            return BigDecimal.ZERO;
        }

        long totalMinutes = java.time.Duration.between(startTime, endTime).toMinutes();
        long hoursCovered = totalMinutes / 60;

        // If coverage was for less than 4 hours, refund 50%
        if (hoursCovered < 4) {
            return premiumPaid.multiply(BigDecimal.valueOf(0.5));
        }

        // If coverage was for 4-8 hours, refund 25%
        if (hoursCovered <= 8) {
            return premiumPaid.multiply(BigDecimal.valueOf(0.25));
        }

        // No refund after 8 hours
        return BigDecimal.ZERO;
    }

    /**
     * Check if current season is monsoon (simplified for India)
     */
    private boolean isMonsoonSeason() {
        int month = java.time.LocalDate.now().getMonthValue();
        // June to September is monsoon season in most of India
        return month >= 6 && month <= 9;
    }

    /**
     * Calculate no-claim bonus discount
     */
    public BigDecimal calculateNoClaimBonus(Integer noClaimYears) {
        if (noClaimYears == null || noClaimYears == 0) {
            return BigDecimal.ONE;
        }

        // 5% discount per no-claim year, capped at 25%
        double discount = Math.min(noClaimYears * 0.05, 0.25);
        return BigDecimal.ONE.subtract(BigDecimal.valueOf(discount));
    }
}