package com.nagarro.eclaims.policy.repository;

import com.nagarro.eclaims.policy.entity.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, UUID> {
    Optional<Policy> findByPolicyNumber(String policyNumber);
    List<Policy> findByCustomerId(UUID customerId);
    Optional<Policy> findByPolicyNumberAndCustomerId(String policyNumber, UUID customerId);
}

