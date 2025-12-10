package com.togglecover.insurance.service;

import com.togglecover.insurance.model.dto.InsurancePlanDTO;
import com.togglecover.insurance.model.entity.InsurancePlan;
import com.togglecover.insurance.repository.InsurancePlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InsurancePlanService {

    private final InsurancePlanRepository insurancePlanRepository;
    private final ModelMapper modelMapper;

    public InsurancePlanDTO getPlanByCode(String planCode) {
        InsurancePlan plan = insurancePlanRepository.findByPlanCodeAndIsActive(planCode, true)
                .orElseThrow(() -> new RuntimeException("Active insurance plan not found with code: " + planCode));
        return convertToDTO(plan);
    }

    public List<InsurancePlanDTO> getAllActivePlans() {
        return insurancePlanRepository.findByIsActive(true).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<InsurancePlanDTO> getPlansByCoverageType(String coverageType) {
        return insurancePlanRepository.findByCoverageType(coverageType).stream()
                .filter(InsurancePlan::getIsActive)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<InsurancePlanDTO> getEligiblePlansByAge(Integer age) {
        return insurancePlanRepository.findEligiblePlansByAge(age).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<InsurancePlanDTO> getPlansWithinBudget(Double maxDailyPremium) {
        return insurancePlanRepository.findPlansWithinBudget(maxDailyPremium).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<InsurancePlanDTO> getPlansWithMinCoverage(Double minCoverage) {
        return insurancePlanRepository.findPlansWithMinCoverage(minCoverage).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<String> getAllCoverageTypes() {
        return insurancePlanRepository.findAllActiveCoverageTypes();
    }

    public InsurancePlanDTO getMostPopularPlan() {
        // For now, return the basic plan as most popular
        // In production, you would track plan popularity
        return insurancePlanRepository.findByPlanCodeAndIsActive("BASIC_DAILY", true)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("No active plans available"));
    }

    @Transactional
    public InsurancePlanDTO createPlan(InsurancePlanDTO planDTO) {
        // Check if plan code already exists
        if (insurancePlanRepository.findByPlanCode(planDTO.getPlanCode()).isPresent()) {
            throw new RuntimeException("Insurance plan with code already exists: " + planDTO.getPlanCode());
        }

        InsurancePlan plan = new InsurancePlan();
        plan.setPlanCode(planDTO.getPlanCode());
        plan.setPlanName(planDTO.getPlanName());
        plan.setDescription(planDTO.getDescription());
        plan.setDailyPremium(planDTO.getDailyPremium());
        plan.setCoverageAmount(planDTO.getCoverageAmount());
        plan.setCoverageType(planDTO.getCoverageType());
        plan.setMaxAge(planDTO.getMaxAge());
        plan.setMinAge(planDTO.getMinAge());
        plan.setWaitingPeriodDays(planDTO.getWaitingPeriodDays());
        plan.setIsActive(true);

        InsurancePlan savedPlan = insurancePlanRepository.save(plan);
        log.info("Created new insurance plan: {} - {}", planDTO.getPlanCode(), planDTO.getPlanName());

        return convertToDTO(savedPlan);
    }

    @Transactional
    public InsurancePlanDTO updatePlan(String planCode, InsurancePlanDTO planDTO) {
        InsurancePlan plan = insurancePlanRepository.findByPlanCode(planCode)
                .orElseThrow(() -> new RuntimeException("Insurance plan not found: " + planCode));

        plan.setPlanName(planDTO.getPlanName());
        plan.setDescription(planDTO.getDescription());
        plan.setDailyPremium(planDTO.getDailyPremium());
        plan.setCoverageAmount(planDTO.getCoverageAmount());
        plan.setCoverageType(planDTO.getCoverageType());
        plan.setMaxAge(planDTO.getMaxAge());
        plan.setMinAge(planDTO.getMinAge());
        plan.setWaitingPeriodDays(planDTO.getWaitingPeriodDays());
        plan.setIsActive(planDTO.getIsActive());

        InsurancePlan updatedPlan = insurancePlanRepository.save(plan);
        log.info("Updated insurance plan: {}", planCode);

        return convertToDTO(updatedPlan);
    }

    @Transactional
    public void deactivatePlan(String planCode) {
        InsurancePlan plan = insurancePlanRepository.findByPlanCode(planCode)
                .orElseThrow(() -> new RuntimeException("Insurance plan not found: " + planCode));

        plan.setIsActive(false);
        insurancePlanRepository.save(plan);

        log.info("Deactivated insurance plan: {}", planCode);
    }

    public BigDecimal calculateAnnualPremium(String planCode) {
        InsurancePlan plan = insurancePlanRepository.findByPlanCode(planCode)
                .orElseThrow(() -> new RuntimeException("Insurance plan not found: " + planCode));

        return plan.getDailyPremium().multiply(BigDecimal.valueOf(365));
    }

    public BigDecimal calculateMonthlyPremium(String planCode) {
        InsurancePlan plan = insurancePlanRepository.findByPlanCode(planCode)
                .orElseThrow(() -> new RuntimeException("Insurance plan not found: " + planCode));

        return plan.getDailyPremium().multiply(BigDecimal.valueOf(30));
    }

    private InsurancePlanDTO convertToDTO(InsurancePlan plan) {
        InsurancePlanDTO dto = modelMapper.map(plan, InsurancePlanDTO.class);
        return dto;
    }
}