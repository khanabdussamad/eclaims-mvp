package com.nagarro.eclaims.workflow.entity;

import com.nagarro.eclaims.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "workflow_transitions", indexes = {
        @Index(name = "idx_workflow_from_to", columnList = "from_status,to_status"),
        @Index(name = "idx_workflow_permission", columnList = "required_permission")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowTransition extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String fromStatus;

    @Column(nullable = false, length = 50)
    private String toStatus;

    @Column(nullable = false, length = 100)
    private String requiredPermission;

    @Column(nullable = false, length = 50)
    private String actorRole;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(length = 500)
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "workflow_preconditions", joinColumns = @JoinColumn(name = "transition_id"))
    @Column(name = "precondition")
    private Set<String> preconditions;
}

