package com.togglecover.insurance.service;

import com.togglecover.insurance.model.dto.CoverageRequest;
import com.togglecover.insurance.model.dto.CoverageResponse;
import com.togglecover.insurance.model.dto.CoverageStatusResponse;
import com.togglecover.insurance.model.entity.CoverageRecord;
import com.togglecover.insurance.model.entity.Policy;
import com.togglecover.insurance.repository.CoverageRecordRepository;
import com.togglecover.insurance.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoverageService {

    private final PolicyRepository policyRepository;
    private final CoverageRecordRepository coverageRecordRepository;
    private final PremiumCalculatorService premiumCalculatorService;

    @Transactional
    public CoverageResponse toggleDailyCoverage(CoverageRequest request) {
        // Validate and get policy
        Policy policy = policyRepository.findByPolicyNumber(request.getPolicyNumber())
                .orElseThrow(() -> new RuntimeException("Policy not found"));

        if (!"ACTIVE".equals(policy.getStatus())) {
            throw new RuntimeException("Policy is not active");
        }

        // Use today's date if not provided
        LocalDate coverageDate = request.getCoverageDate() != null ?
                request.getCoverageDate() : LocalDate.now();

        // Check if coverage record already exists for today
        Optional<CoverageRecord> existingRecord = coverageRecordRepository
                .findByPolicyIdAndCoverageDate(policy.getId(), coverageDate);

        CoverageRecord coverageRecord;

        if (existingRecord.isPresent()) {
            coverageRecord = existingRecord.get();

            // Check if already active and trying to activate again
            if (Boolean.TRUE.equals(request.getToggleCoverage()) &&
                    Boolean.TRUE.equals(coverageRecord.getIsActive())) {
                throw new RuntimeException("Coverage is already active for today");
            }

            // Check if already inactive and trying to deactivate
            if (Boolean.FALSE.equals(request.getToggleCoverage()) &&
                    Boolean.FALSE.equals(coverageRecord.getIsActive())) {
                throw new RuntimeException("Coverage is already inactive for today");
            }
        } else {
            // Create new coverage record
            coverageRecord = new CoverageRecord();
            coverageRecord.setPolicy(policy);
            coverageRecord.setCoverageDate(coverageDate);
            coverageRecord.setCoverageAmount(policy.getPlan().getCoverageAmount());
        }

        // Toggle coverage
        if (Boolean.TRUE.equals(request.getToggleCoverage())) {
            return activateCoverage(coverageRecord, request, policy);
        } else {
            return deactivateCoverage(coverageRecord);
        }
    }

    private CoverageResponse activateCoverage(CoverageRecord coverageRecord,
                                              CoverageRequest request,
                                              Policy policy) {
        // Calculate premium with weather risk multiplier
        BigDecimal calculatedPremium = premiumCalculatorService.calculateDailyPremium(
                policy.getPlan().getDailyPremium(),
                request.getTemperature(),
                request.getLocation(),
                request.getGigPlatform()
        );

        // Check wallet balance
        if (policy.getWalletBalance().compareTo(calculatedPremium) < 0) {
            throw new RuntimeException("Insufficient wallet balance to activate coverage");
        }

        // Deduct premium from wallet
        policy.setWalletBalance(policy.getWalletBalance().subtract(calculatedPremium));
        policy.setTotalPremiumPaid(policy.getTotalPremiumPaid().add(calculatedPremium));

        // Update coverage record
        coverageRecord.setStartTime(LocalDateTime.now());
        coverageRecord.setPremiumAmount(calculatedPremium);
        coverageRecord.setStatus("ACTIVE");
        coverageRecord.setIsActive(true);
        coverageRecord.setLocation(request.getLocation());
        coverageRecord.setGigPlatform(request.getGigPlatform());
        coverageRecord.setWeatherRiskMultiplier(
                premiumCalculatorService.calculateWeatherRiskMultiplier(
                        request.getTemperature(),
                        request.getLocation()
                )
        );

        coverageRecordRepository.save(coverageRecord);
        policyRepository.save(policy);

        log.info("Coverage activated for policy: {} on date: {}. Premium charged: {}",
                policy.getPolicyNumber(), coverageRecord.getCoverageDate(), calculatedPremium);

        return createCoverageResponse(coverageRecord,
                "Coverage activated successfully",
                calculatedPremium.doubleValue());
    }

    private CoverageResponse deactivateCoverage(CoverageRecord coverageRecord) {
        coverageRecord.setEndTime(LocalDateTime.now());
        coverageRecord.setStatus("INACTIVE");
        coverageRecord.setIsActive(false);

        // Note: Premium is not refunded on deactivation (as per business rules)

        coverageRecordRepository.save(coverageRecord);

        log.info("Coverage deactivated for policy: {} on date: {}",
                coverageRecord.getPolicy().getPolicyNumber(),
                coverageRecord.getCoverageDate());

        return createCoverageResponse(coverageRecord,
                "Coverage deactivated successfully",
                coverageRecord.getPremiumAmount() != null ?
                        coverageRecord.getPremiumAmount().doubleValue() : 0.0);
    }

    public CoverageStatusResponse getCoverageStatus(String policyNumber, LocalDate date) {
        Policy policy = policyRepository.findByPolicyNumber(policyNumber)
                .orElseThrow(() -> new RuntimeException("Policy not found"));

        LocalDate checkDate = date != null ? date : LocalDate.now();

        Optional<CoverageRecord> coverageRecord = coverageRecordRepository
                .findActiveCoverageOnDate(policy.getId(), checkDate);

        CoverageStatusResponse response = new CoverageStatusResponse();
        response.setPolicyNumber(policyNumber);
        response.setCoverageDate(checkDate);
        response.setWalletBalance(policy.getWalletBalance().doubleValue());

        if (coverageRecord.isPresent()) {
            CoverageRecord record = coverageRecord.get();
            response.setIsCoverageActiveToday(true);
            response.setCurrentStatus(record.getStatus());
            response.setPremiumPaidToday(record.getPremiumAmount() != null ?
                    record.getPremiumAmount().doubleValue() : 0.0);
            response.setGigPlatform(record.getGigPlatform());
            response.setLocation(record.getLocation());
        } else {
            response.setIsCoverageActiveToday(false);
            response.setCurrentStatus("INACTIVE");
            response.setPremiumPaidToday(0.0);
        }

        return response;
    }

    private CoverageResponse createCoverageResponse(CoverageRecord record,
                                                    String message,
                                                    Double premiumCharged) {
        CoverageResponse response = new CoverageResponse();
        response.setPolicyNumber(record.getPolicy().getPolicyNumber());
        response.setStatus(record.getStatus());
        response.setCoverageActive(record.getIsActive());
        response.setMessage(message);
        response.setPremiumCharged(premiumCharged);
        response.setCoverageAmount(record.getCoverageAmount() != null ?
                record.getCoverageAmount().doubleValue() : 0.0);
        response.setCoverageDate(record.getCoverageDate());
        response.setStartTime(record.getStartTime());
        response.setEndTime(record.getEndTime());
        response.setGigPlatform(record.getGigPlatform());
        return response;
    }
}