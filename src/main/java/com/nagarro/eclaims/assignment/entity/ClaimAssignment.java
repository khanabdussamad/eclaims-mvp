package com.nagarro.eclaims.assignment.entity;

import com.nagarro.eclaims.common.entity.BaseEntity;
import com.nagarro.eclaims.claim.entity.Claim;
import com.nagarro.eclaims.assignment.enums.AssignmentRole;
import com.nagarro.eclaims.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "claim_assignments", indexes = {
        @Index(name = "idx_assignment_claim", columnList = "claim_id"),
        @Index(name = "idx_assignment_user", columnList = "assigned_user_id"),
        @Index(name = "idx_assignment_role", columnList = "assignment_role"),
        @Index(name = "idx_assignment_active", columnList = "is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimAssignment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claim_id", nullable = false)
    private Claim claim;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_user_id", nullable = false)
    private User assignedUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AssignmentRole assignmentRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by_user_id")
    private User assignedByUser;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column
    private Instant assignedAt;

    @Column
    private Instant completedAt;

    @Column(length = 500)
    private String completionNotes;

    @Column
    private Integer sequenceNumber; // For cases with multiple assignments
}

