package com.togglecover.insurance.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "coverage_records",
        uniqueConstraints = @UniqueConstraint(columnNames = {"policy_id", "coverage_date"}))
@Data
public class CoverageRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;

    @Column(nullable = false)
    private LocalDate coverageDate;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Column(length = 20)
    private String status = "INACTIVE"; // ACTIVE, INACTIVE, PENDING, CANCELLED

    @Column(precision = 10, scale = 2)
    private BigDecimal premiumAmount;

    @Column(precision = 15, scale = 2)
    private BigDecimal coverageAmount;

    @Column(precision = 5, scale = 2)
    private BigDecimal weatherRiskMultiplier = BigDecimal.ONE;

    private String location;

    @Column(length = 50)
    private String gigPlatform; // SWIGGY, ZOMATO, ZEPTO, UBER_EATS

    private Boolean isActive = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}