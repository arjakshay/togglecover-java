package com.togglecover.insurance.service;

import com.togglecover.insurance.model.dto.CreatePolicyRequest;
import com.togglecover.insurance.model.dto.PolicyDTO;
import com.togglecover.insurance.model.dto.RenewPolicyRequest;
import com.togglecover.insurance.model.entity.InsurancePlan;
import com.togglecover.insurance.model.entity.Policy;
import com.togglecover.insurance.repository.InsurancePlanRepository;
import com.togglecover.insurance.repository.PolicyRepository;
import com.togglecover.insurance.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PolicyService {

    private final PolicyRepository policyRepository;
    private final InsurancePlanRepository planRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public PolicyDTO createPolicy(CreatePolicyRequest request) {
        // Get current user ID from authentication context
        Long currentUserId = UserContext.getCurrentUserId();

        if (currentUserId == null) {
            throw new RuntimeException("User not authenticated. Please login first.");
        }

        log.info("Creating policy for authenticated user ID: {}", currentUserId);

        // Override user ID from request with authenticated user ID (for security)
        request.setUserId(currentUserId);

        // Check if user already has an active policy
        Long activePolicies = policyRepository.countActivePoliciesByUser(currentUserId);
        if (activePolicies > 0) {
            throw new RuntimeException("User already has an active policy. You can only have one active policy at a time.");
        }

        // Find insurance plan
        InsurancePlan plan = planRepository.findByPlanCode(request.getPlanCode())
                .orElseThrow(() -> new RuntimeException("Insurance plan not found with code: " + request.getPlanCode()));

        if (!plan.getIsActive()) {
            throw new RuntimeException("Insurance plan is not active: " + plan.getPlanName());
        }

        // Generate unique policy number
        String policyNumber = generatePolicyNumber();

        // Create policy
        Policy policy = new Policy();
        policy.setPolicyNumber(policyNumber);
        policy.setUserId(currentUserId);
        policy.setPlan(plan);
        policy.setStartDate(LocalDate.now());
        policy.setEndDate(LocalDate.now().plusYears(1)); // Default 1 year policy
        policy.setStatus("ACTIVE");
        policy.setAutoRenew(request.getAutoRenew() != null ? request.getAutoRenew() : true);

        // Set initial wallet balance
        if (request.getInitialWalletTopUp() != null && request.getInitialWalletTopUp().compareTo(BigDecimal.ZERO) > 0) {
            policy.setWalletBalance(request.getInitialWalletTopUp());
            log.info("Initial wallet top-up: {} for policy: {}", request.getInitialWalletTopUp(), policyNumber);
        } else {
            policy.setWalletBalance(BigDecimal.ZERO);
        }

        Policy savedPolicy = policyRepository.save(policy);
        log.info("Created new policy: {} for user: {}, Plan: {}",
                policyNumber, currentUserId, plan.getPlanName());

        return convertToDTO(savedPolicy);
    }

    public PolicyDTO getPolicyByNumber(String policyNumber) {
        Policy policy = policyRepository.findByPolicyNumber(policyNumber)
                .orElseThrow(() -> new RuntimeException("Policy not found: " + policyNumber));

        // Check if current user is authorized to view this policy
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId != null && !policy.getUserId().equals(currentUserId)) {
            // Check if user is admin (you can add admin check here)
            log.warn("User {} attempted to access policy {} owned by user {}",
                    currentUserId, policyNumber, policy.getUserId());
            throw new RuntimeException("Access denied. You are not authorized to view this policy.");
        }

        return convertToDTO(policy);
    }

    public List<PolicyDTO> getCurrentUserPolicies() {
        Long currentUserId = UserContext.getCurrentUserId();

        if (currentUserId == null) {
            throw new RuntimeException("User not authenticated. Please login first.");
        }

        log.debug("Fetching policies for user: {}", currentUserId);

        return policyRepository.findByUserId(currentUserId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<PolicyDTO> getPoliciesByUser(Long userId) {
        // This method is typically for admin use
        // For now, we'll allow it, but you can add role-based checks

        return policyRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public PolicyDTO getActivePolicyForCurrentUser() {
        Long currentUserId = UserContext.getCurrentUserId();

        if (currentUserId == null) {
            throw new RuntimeException("User not authenticated. Please login first.");
        }

        return policyRepository.findActivePolicyByUser(currentUserId)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("No active policy found for user: " + currentUserId));
    }

    @Transactional
    public PolicyDTO renewPolicy(RenewPolicyRequest request) {
        Policy policy = policyRepository.findByPolicyNumber(request.getPolicyNumber())
                .orElseThrow(() -> new RuntimeException("Policy not found: " + request.getPolicyNumber()));

        // Check if current user is authorized to renew this policy
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId != null && !policy.getUserId().equals(currentUserId)) {
            throw new RuntimeException("Access denied. You are not authorized to renew this policy.");
        }

        if (!"ACTIVE".equals(policy.getStatus()) && !"EXPIRED".equals(policy.getStatus())) {
            throw new RuntimeException("Policy cannot be renewed. Current status: " + policy.getStatus());
        }

        // Extend end date
        LocalDate newEndDate = policy.getEndDate().plusMonths(
                request.getRenewalMonths() != null ? request.getRenewalMonths() : 12
        );
        policy.setEndDate(newEndDate);
        policy.setStatus("ACTIVE");

        // Add wallet top-up if provided
        if (request.getWalletTopUp() != null && request.getWalletTopUp().compareTo(BigDecimal.ZERO) > 0) {
            policy.setWalletBalance(policy.getWalletBalance().add(request.getWalletTopUp()));
            log.info("Wallet topped up by {} during policy renewal for policy: {}",
                    request.getWalletTopUp(), request.getPolicyNumber());
        }

        Policy updatedPolicy = policyRepository.save(policy);
        log.info("Renewed policy: {} until {}", request.getPolicyNumber(), newEndDate);

        return convertToDTO(updatedPolicy);
    }

    @Transactional
    public PolicyDTO updateWalletBalance(String policyNumber, BigDecimal amount) {
        Policy policy = policyRepository.findByPolicyNumber(policyNumber)
                .orElseThrow(() -> new RuntimeException("Policy not found: " + policyNumber));

        // Check if current user is authorized to update wallet for this policy
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId != null && !policy.getUserId().equals(currentUserId)) {
            throw new RuntimeException("Access denied. You are not authorized to update this wallet.");
        }

        BigDecimal newBalance = policy.getWalletBalance().add(amount);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Insufficient wallet balance. Current balance: " +
                    policy.getWalletBalance() + ", Attempted deduction: " + amount.abs());
        }

        policy.setWalletBalance(newBalance);
        Policy updatedPolicy = policyRepository.save(policy);

        log.info("Updated wallet balance for policy: {} by {}. New balance: {}",
                policyNumber, amount, newBalance);

        return convertToDTO(updatedPolicy);
    }

    public BigDecimal getWalletBalance(String policyNumber) {
        Policy policy = policyRepository.findByPolicyNumber(policyNumber)
                .orElseThrow(() -> new RuntimeException("Policy not found: " + policyNumber));

        // Check if current user is authorized to view wallet balance
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId != null && !policy.getUserId().equals(currentUserId)) {
            throw new RuntimeException("Access denied. You are not authorized to view this wallet balance.");
        }

        return policy.getWalletBalance();
    }

    public BigDecimal getCurrentUserWalletBalance() {
        Long currentUserId = UserContext.getCurrentUserId();

        if (currentUserId == null) {
            throw new RuntimeException("User not authenticated. Please login first.");
        }

        // Get user's active policy
        Policy policy = policyRepository.findActivePolicyByUser(currentUserId)
                .orElseThrow(() -> new RuntimeException("No active policy found for user: " + currentUserId));

        return policy.getWalletBalance();
    }

    @Transactional
    public PolicyDTO cancelPolicy(String policyNumber) {
        Policy policy = policyRepository.findByPolicyNumber(policyNumber)
                .orElseThrow(() -> new RuntimeException("Policy not found: " + policyNumber));

        // Check if current user is authorized to cancel this policy
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId != null && !policy.getUserId().equals(currentUserId)) {
            throw new RuntimeException("Access denied. You are not authorized to cancel this policy.");
        }

        if (!"ACTIVE".equals(policy.getStatus())) {
            throw new RuntimeException("Policy cannot be cancelled. Current status: " + policy.getStatus());
        }

        policy.setStatus("CANCELLED");
        policy.setAutoRenew(false);
        Policy updatedPolicy = policyRepository.save(policy);

        log.info("Cancelled policy: {} for user: {}", policyNumber, currentUserId);

        return convertToDTO(updatedPolicy);
    }

    private String generatePolicyNumber() {
        return "POL" + LocalDate.now().getYear() +
                String.format("%02d", LocalDate.now().getMonthValue()) +
                UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private PolicyDTO convertToDTO(Policy policy) {
        PolicyDTO dto = modelMapper.map(policy, PolicyDTO.class);
        dto.setPlanId(policy.getPlan().getId());
        dto.setPlanName(policy.getPlan().getPlanName());
        dto.setPlanCode(policy.getPlan().getPlanCode());
        dto.setDailyPremium(policy.getPlan().getDailyPremium());
        dto.setCoverageAmount(policy.getPlan().getCoverageAmount());
        dto.setCoverageType(policy.getPlan().getCoverageType());

        // Calculate days remaining
        if (policy.getEndDate() != null) {
            long daysRemaining = LocalDate.now().until(policy.getEndDate()).getDays();
            dto.setDaysRemaining(Math.max(0, daysRemaining));
        }

        return dto;
    }
}