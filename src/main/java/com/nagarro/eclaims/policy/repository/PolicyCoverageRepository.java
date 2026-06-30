package com.nagarro.eclaims.policy.repository;

import com.nagarro.eclaims.policy.entity.PolicyCoverage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PolicyCoverageRepository extends JpaRepository<PolicyCoverage, UUID> {
    List<PolicyCoverage> findByPolicyId(UUID policyId);
}

