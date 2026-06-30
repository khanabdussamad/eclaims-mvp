package com.nagarro.eclaims.user.repository;

import com.nagarro.eclaims.user.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    Optional<Customer> findByUserId(UUID userId);
    Optional<Customer> findByCustomerNumber(String customerNumber);
    boolean existsByEmail(String email);
}

