package com.togglecover.insurance.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "insurance_plans")
@Data
public class InsurancePlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String planCode;

    @Column(nullable = false)
    private String planName;

    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal dailyPremium;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal coverageAmount;

    @Column(nullable = false)
    private String coverageType; // ACCIDENT, HEALTH, COMPREHENSIVE

    private Integer maxAge;
    private Integer minAge;

    @Column(name = "waiting_period_days")
    private Integer waitingPeriodDays = 30;

    private Boolean isActive = true;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}