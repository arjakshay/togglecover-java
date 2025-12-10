package com.togglecover.insurance.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "policies")
@Data
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String policyNumber;

    @Column(nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private InsurancePlan plan;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    @Column(length = 20)
    private String status = "ACTIVE"; // ACTIVE, EXPIRED, CANCELLED, SUSPENDED

    @Column(precision = 15, scale = 2)
    private BigDecimal totalPremiumPaid = BigDecimal.ZERO;

    private Integer totalClaims = 0;

    @Column(precision = 15, scale = 2)
    private BigDecimal totalClaimsAmount = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2)
    private BigDecimal walletBalance = BigDecimal.ZERO;

    private Boolean autoRenew = true;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}