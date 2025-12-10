package com.togglecover.insurance.controller;

import com.togglecover.insurance.model.dto.CreatePolicyRequest;
import com.togglecover.insurance.model.dto.PolicyDTO;
import com.togglecover.insurance.model.dto.RenewPolicyRequest;
import com.togglecover.insurance.service.PolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/insurance/policies")
@RequiredArgsConstructor
@Tag(name = "Policy Management", description = "APIs for managing insurance policies")
public class PolicyController {

    private final PolicyService policyService;

    @PostMapping
    @Operation(summary = "Create a new insurance policy")
    public ResponseEntity<PolicyDTO> createPolicy(@Valid @RequestBody CreatePolicyRequest request) {
        PolicyDTO policy = policyService.createPolicy(request);
        return ResponseEntity.ok(policy);
    }

    @GetMapping("/{policyNumber}")
    @Operation(summary = "Get policy by policy number")
    public ResponseEntity<PolicyDTO> getPolicy(@PathVariable String policyNumber) {
        PolicyDTO policy = policyService.getPolicyByNumber(policyNumber);
        return ResponseEntity.ok(policy);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all policies for a user")
    public ResponseEntity<List<PolicyDTO>> getUserPolicies(@PathVariable Long userId) {
        List<PolicyDTO> policies = policyService.getPoliciesByUser(userId);
        return ResponseEntity.ok(policies);
    }

    @PostMapping("/renew")
    @Operation(summary = "Renew an existing policy")
    public ResponseEntity<PolicyDTO> renewPolicy(@Valid @RequestBody RenewPolicyRequest request) {
        PolicyDTO policy = policyService.renewPolicy(request);
        return ResponseEntity.ok(policy);
    }

    @PostMapping("/{policyNumber}/wallet/topup")
    @Operation(summary = "Top up wallet balance")
    public ResponseEntity<PolicyDTO> topUpWallet(
            @PathVariable String policyNumber,
            @RequestParam BigDecimal amount) {
        PolicyDTO policy = policyService.updateWalletBalance(policyNumber, amount);
        return ResponseEntity.ok(policy);
    }

    @GetMapping("/{policyNumber}/wallet/balance")
    @Operation(summary = "Get wallet balance")
    public ResponseEntity<BigDecimal> getWalletBalance(@PathVariable String policyNumber) {
        BigDecimal balance = policyService.getWalletBalance(policyNumber);
        return ResponseEntity.ok(balance);
    }

    @GetMapping("/health")
    @Operation(summary = "Health check endpoint")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Insurance Service is running");
    }
}