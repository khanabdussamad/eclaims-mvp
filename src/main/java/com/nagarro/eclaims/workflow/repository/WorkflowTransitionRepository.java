package com.nagarro.eclaims.workflow.repository;

import com.nagarro.eclaims.workflow.entity.WorkflowTransition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkflowTransitionRepository extends JpaRepository<WorkflowTransition, UUID> {

    Optional<WorkflowTransition> findByFromStatusAndToStatusAndActiveTrue(String fromStatus, String toStatus);

    @Query("SELECT wt FROM WorkflowTransition wt WHERE wt.fromStatus = :fromStatus AND wt.active = true")
    List<WorkflowTransition> findAvailableTransitionsFrom(@Param("fromStatus") String fromStatus);

    @Query("SELECT wt FROM WorkflowTransition wt WHERE wt.fromStatus = :fromStatus " +
            "AND wt.toStatus = :toStatus AND wt.active = true")
    Optional<WorkflowTransition> findTransition(@Param("fromStatus") String fromStatus,
                                                 @Param("toStatus") String toStatus);

    @Query("SELECT wt FROM WorkflowTransition wt WHERE wt.fromStatus = :fromStatus " +
            "AND wt.requiredPermission = :permission AND wt.active = true")
    List<WorkflowTransition> findByFromStatusAndPermission(@Param("fromStatus") String fromStatus,
                                                             @Param("permission") String permission);
}

