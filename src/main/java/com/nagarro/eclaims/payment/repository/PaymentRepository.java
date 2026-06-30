package com.nagarro.eclaims.payment.repository;

import com.nagarro.eclaims.payment.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    @Query("SELECT p FROM Payment p WHERE p.claim.id = :claimId ORDER BY p.initiatedAt DESC")
    List<Payment> findByClaimId(@Param("claimId") UUID claimId);

    @Query("SELECT p FROM Payment p WHERE p.claim.id = :claimId AND p.paymentStatus = :status")
    Optional<Payment> findByClaimIdAndStatus(@Param("claimId") UUID claimId, @Param("status") String status);

    @Query("SELECT p FROM Payment p WHERE p.paymentStatus = 'COMPLETED' ORDER BY p.completedAt DESC")
    Page<Payment> findCompletedPayments(Pageable pageable);

    @Query("SELECT p FROM Payment p WHERE p.paymentStatus = 'PENDING' ORDER BY p.initiatedAt")
    List<Payment> findPendingPayments();

    @Query("SELECT p FROM Payment p WHERE p.externalPaymentGatewayId = :gatewayId")
    Optional<Payment> findByExternalGatewayId(@Param("gatewayId") String gatewayId);
}

