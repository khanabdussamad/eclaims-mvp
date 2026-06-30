package com.nagarro.eclaims.claim.service;

import com.nagarro.eclaims.claim.dto.*;
import com.nagarro.eclaims.claim.entity.Claim;
import com.nagarro.eclaims.claim.entity.ClaimStatusHistory;
import com.nagarro.eclaims.claim.enums.ClaimStatus;
import com.nagarro.eclaims.claim.repository.ClaimRepository;
import com.nagarro.eclaims.claim.repository.ClaimStatusHistoryRepository;
import com.nagarro.eclaims.common.exception.AccessDeniedBusinessException;
import com.nagarro.eclaims.common.exception.BusinessException;
import com.nagarro.eclaims.common.exception.ResourceNotFoundException;
import com.nagarro.eclaims.policy.entity.Policy;
import com.nagarro.eclaims.policy.repository.PolicyRepository;
import com.nagarro.eclaims.user.entity.Customer;
import com.nagarro.eclaims.user.entity.User;
import com.nagarro.eclaims.user.repository.CustomerRepository;
import com.nagarro.eclaims.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final ClaimStatusHistoryRepository claimStatusHistoryRepository;
    private final PolicyRepository policyRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final ClaimNumberGenerator claimNumberGenerator;

    public ClaimService(ClaimRepository claimRepository,
                       ClaimStatusHistoryRepository claimStatusHistoryRepository,
                       PolicyRepository policyRepository,
                       UserRepository userRepository,
                       CustomerRepository customerRepository,
                       ClaimNumberGenerator claimNumberGenerator) {
        this.claimRepository = claimRepository;
        this.claimStatusHistoryRepository = claimStatusHistoryRepository;
        this.policyRepository = policyRepository;
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.claimNumberGenerator = claimNumberGenerator;
    }

    public ClaimCreatedResponse createClaim(CreateClaimRequest request, UUID customerId) {
        log.info("Creating claim for customer: {}", customerId);

        // Validate policy exists and is active
        Policy policy = policyRepository.findByPolicyNumber(request.policyNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Policy", request.policyNumber()));

        if (!"ACTIVE".equals(policy.getPolicyStatus())) {
            throw new BusinessException("POLICY_NOT_ACTIVE", "Policy must be active to create claim");
        }

        // Validate customer owns policy
        if (!policy.getCustomer().getId().equals(customerId)) {
            throw new AccessDeniedBusinessException("POLICY_NOT_OWNED", "Customer does not own this policy");
        }

        // Validate incident date is not in future
        if (request.incidentDate().isAfter(LocalDate.now())) {
            throw new BusinessException("INCIDENT_DATE_IN_FUTURE", "Incident date cannot be in the future");
        }

        // Get customer entity
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", customerId.toString()));

        // Generate claim number
        String claimNumber = claimNumberGenerator.generateClaimNumber();

        // Create claim
        Claim claim = Claim.builder()
                .claimNumber(claimNumber)
                .policy(policy)
                .customer(customer)
                .claimType(request.claimType())
                .currentStatus(ClaimStatus.SUBMITTED.toString())
                .incidentDate(request.incidentDate())
                .incidentTime(request.incidentTime())
                .incidentAddressLine1(request.incidentLocation().addressLine1())
                .incidentCity(request.incidentLocation().city())
                .incidentState(request.incidentLocation().state())
                .incidentZipCode(request.incidentLocation().zipCode())
                .incidentCountry(request.incidentLocation().country())
                .description(request.description())
                .vehicleDrivable(request.vehicleDrivable())
                .policeReportAvailable(request.policeReportAvailable())
                .submittedAt(Instant.now())
                .build();

        claim = claimRepository.save(claim);

        // Create status history entry
        User currentUser = userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", customerId.toString()));

        ClaimStatusHistory history = ClaimStatusHistory.builder()
                .claim(claim)
                .fromStatus(null)
                .toStatus(ClaimStatus.SUBMITTED.toString())
                .changedByUser(currentUser)
                .changedByRole("CUSTOMER")
                .reason("Claim submitted")
                .createdAt(Instant.now())
                .build();

        claimStatusHistoryRepository.save(history);

        log.info("Claim created successfully: {}", claimNumber);

        return new ClaimCreatedResponse(
                claim.getId().toString(),
                claim.getClaimNumber(),
                claim.getCurrentStatus(),
                claim.getCreatedAt()
        );
    }

    public ClaimDetailResponse getClaimDetail(UUID claimId, UUID userId) {
        log.info("Getting claim details for claim: {} by user: {}", claimId, userId);

        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim", claimId.toString()));

        // Validate user access
        validateClaimAccess(claim, userId);

        return mapToClaimDetail(claim);
    }

    public Page<ClaimListItemResponse> listClaims(UUID customerId, Pageable pageable) {
        log.info("Listing claims for customer: {}", customerId);

        Page<Claim> claims = claimRepository.findByCustomerId(customerId, pageable);

        List<ClaimListItemResponse> content = claims.getContent().stream()
                .map(this::mapToClaimListItem)
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, claims.getTotalElements());
    }

    public List<ClaimStatusHistory> getClaimTimeline(UUID claimId, UUID userId) {
        log.info("Getting timeline for claim: {}", claimId);

        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim", claimId.toString()));

        // Validate user access
        validateClaimAccess(claim, userId);

        return claimStatusHistoryRepository.findByClaimIdOrderByCreatedAtDesc(claimId);
    }

    private void validateClaimAccess(Claim claim, UUID userId) {
        // Customer can only view own claims
        if (!claim.getCustomer().getUser().getId().equals(userId)) {
            throw new AccessDeniedBusinessException("CLAIM_ACCESS", "You do not have access to this claim");
        }
    }

    private ClaimDetailResponse mapToClaimDetail(Claim claim) {
        PolicySummaryResponse policy = new PolicySummaryResponse(
                claim.getPolicy().getPolicyNumber(),
                claim.getPolicy().getCoverageLimit().doubleValue(),
                claim.getPolicy().getDeductibleAmount().doubleValue()
        );

        CustomerSummaryResponse customer = new CustomerSummaryResponse(
                claim.getCustomer().getCustomerNumber(),
                claim.getCustomer().getFirstName() + " " + claim.getCustomer().getLastName()
        );

        IncidentResponse incident = new IncidentResponse(
                claim.getIncidentDate(),
                claim.getIncidentTime(),
                claim.getIncidentAddressLine1(),
                claim.getIncidentCity(),
                claim.getIncidentState(),
                claim.getIncidentZipCode(),
                claim.getIncidentCountry(),
                claim.getDescription()
        );

        return new ClaimDetailResponse(
                claim.getId().toString(),
                claim.getClaimNumber(),
                claim.getCurrentStatus(),
                claim.getClaimType(),
                policy,
                customer,
                incident,
                claim.getVehicleDrivable(),
                claim.getPoliceReportAvailable(),
                claim.getSubmittedAt()
        );
    }

    private ClaimListItemResponse mapToClaimListItem(Claim claim) {
        return new ClaimListItemResponse(
                claim.getId().toString(),
                claim.getClaimNumber(),
                claim.getPolicy().getPolicyNumber(),
                claim.getCustomer().getFirstName() + " " + claim.getCustomer().getLastName(),
                claim.getClaimType(),
                claim.getCurrentStatus(),
                claim.getIncidentDate(),
                claim.getSubmittedAt()
        );
    }
}

