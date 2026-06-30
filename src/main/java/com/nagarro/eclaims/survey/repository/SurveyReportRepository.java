package com.nagarro.eclaims.survey.repository;

import com.nagarro.eclaims.survey.entity.SurveyReport;
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
public interface SurveyReportRepository extends JpaRepository<SurveyReport, UUID> {

    @Query("SELECT sr FROM SurveyReport sr WHERE sr.claim.id = :claimId")
    Optional<SurveyReport> findByClaimId(@Param("claimId") UUID claimId);

    @Query("SELECT sr FROM SurveyReport sr WHERE sr.surveyorUser.id = :surveyorId " +
           "AND sr.surveyStatus = 'SUBMITTED' ORDER BY sr.submittedAt DESC")
    Page<SurveyReport> findSubmittedBySurveyor(@Param("surveyorId") UUID surveyorId, Pageable pageable);

    @Query("SELECT sr FROM SurveyReport sr WHERE sr.surveyorUser.id = :surveyorId " +
           "ORDER BY sr.surveyDate DESC")
    Page<SurveyReport> findBySurveyor(@Param("surveyorId") UUID surveyorId, Pageable pageable);

    @Query("SELECT sr FROM SurveyReport sr WHERE sr.surveyStatus = 'PENDING' " +
           "ORDER BY sr.surveyDate")
    List<SurveyReport> findPendingSurveys();

    @Query("SELECT sr FROM SurveyReport sr WHERE sr.surveyorUser.id = :surveyorId " +
           "AND sr.surveyStatus = 'PENDING' ORDER BY sr.surveyDate")
    List<SurveyReport> findPendingForSurveyor(@Param("surveyorId") UUID surveyorId);
}

