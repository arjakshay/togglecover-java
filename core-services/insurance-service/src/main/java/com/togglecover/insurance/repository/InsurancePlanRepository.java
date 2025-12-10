package com.togglecover.insurance.repository;

import com.togglecover.insurance.model.entity.InsurancePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InsurancePlanRepository extends JpaRepository<InsurancePlan, Long> {

    Optional<InsurancePlan> findByPlanCode(String planCode);

    List<InsurancePlan> findByIsActive(Boolean isActive);

    List<InsurancePlan> findByCoverageType(String coverageType);

    @Query("SELECT p FROM InsurancePlan p WHERE p.isActive = true AND p.minAge <= :age AND (p.maxAge IS NULL OR p.maxAge >= :age)")
    List<InsurancePlan> findEligiblePlansByAge(@Param("age") Integer age);

    @Query("SELECT p FROM InsurancePlan p WHERE p.isActive = true AND p.dailyPremium <= :maxDailyPremium")
    List<InsurancePlan> findPlansWithinBudget(@Param("maxDailyPremium") Double maxDailyPremium);

    @Query("SELECT p FROM InsurancePlan p WHERE p.isActive = true AND p.coverageAmount >= :minCoverage")
    List<InsurancePlan> findPlansWithMinCoverage(@Param("minCoverage") Double minCoverage);

    @Query("SELECT DISTINCT p.coverageType FROM InsurancePlan p WHERE p.isActive = true")
    List<String> findAllActiveCoverageTypes();

    Optional<InsurancePlan> findByPlanCodeAndIsActive(String planCode, Boolean isActive);

    @Query("SELECT COUNT(p) FROM InsurancePlan p WHERE p.isActive = true")
    Long countActivePlans();

    @Query("SELECT p FROM InsurancePlan p WHERE p.isActive = true ORDER BY p.dailyPremium ASC")
    List<InsurancePlan> findAllActivePlansSortedByPremium();
}