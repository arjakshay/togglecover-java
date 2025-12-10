package com.togglecover.insurance.repository;

import com.togglecover.insurance.model.entity.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long> {

    Optional<Policy> findByPolicyNumber(String policyNumber);

    List<Policy> findByUserId(Long userId);

    List<Policy> findByUserIdAndStatus(Long userId, String status);

    Optional<Policy> findByUserIdAndPolicyNumber(Long userId, String policyNumber);

    @Query("SELECT p FROM Policy p WHERE p.userId = :userId AND p.status = 'ACTIVE'")
    Optional<Policy> findActivePolicyByUser(@Param("userId") Long userId);

    List<Policy> findByEndDateBeforeAndStatus(LocalDate date, String status);

    @Query("SELECT COUNT(p) FROM Policy p WHERE p.userId = :userId AND p.status = 'ACTIVE'")
    Long countActivePoliciesByUser(@Param("userId") Long userId);
}