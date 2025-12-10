package com.togglecover.insurance.repository;

import com.togglecover.insurance.model.entity.CoverageRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CoverageRecordRepository extends JpaRepository<CoverageRecord, Long> {

    Optional<CoverageRecord> findByPolicyIdAndCoverageDate(Long policyId, LocalDate coverageDate);

    List<CoverageRecord> findByPolicyId(Long policyId);

    List<CoverageRecord> findByPolicyIdAndCoverageDateBetween(Long policyId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT cr FROM CoverageRecord cr WHERE cr.policy.id = :policyId AND cr.isActive = true AND cr.coverageDate = :date")
    Optional<CoverageRecord> findActiveCoverageOnDate(@Param("policyId") Long policyId, @Param("date") LocalDate date);

    @Query("SELECT cr FROM CoverageRecord cr WHERE cr.policy.userId = :userId AND cr.coverageDate = :date AND cr.isActive = true")
    List<CoverageRecord> findActiveCoveragesByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Query("SELECT cr FROM CoverageRecord cr WHERE cr.policy.userId = :userId AND cr.coverageDate BETWEEN :startDate AND :endDate")
    List<CoverageRecord> findCoveragesByUserAndDateRange(@Param("userId") Long userId,
                                                         @Param("startDate") LocalDate startDate,
                                                         @Param("endDate") LocalDate endDate);
}