package com.nagarro.eclaims.claim.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Year;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class ClaimNumberGenerator {

    private final AtomicLong sequence = new AtomicLong(0);

    public String generateClaimNumber() {
        int year = Year.now().getValue();
        long seqNum = sequence.incrementAndGet();
        String claimNumber = String.format("CLM-%d-%06d", year, seqNum);
        log.debug("Generated claim number: {}", claimNumber);
        return claimNumber;
    }
}

