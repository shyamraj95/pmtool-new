package com.api.pmtool.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import com.api.pmtool.dtos.DemandCountResponseDTO;
import com.api.pmtool.dtos.SearchDemandResponseDto;
import com.api.pmtool.entity.Demand;
import com.api.pmtool.enums.Status;
import java.time.LocalDate;

@Repository
public interface DemandRepository extends JpaRepository<Demand, UUID> {

        @Query("SELECT DISTINCT d FROM Demand d LEFT JOIN FETCH d.comments WHERE d.id = :demandId")
        Optional<Demand> findByIdWithCommentsAndUploads(@Param("demandId") UUID demandId);

        @Query("SELECT DISTINCT d FROM Demand d LEFT JOIN FETCH d.comments")
        Optional<Demand> getAllDemands();

        Optional<Demand> findById(@Param("demandId") @NonNull UUID demandId);

        @Query(value = "SELECT d.id AS id, " +
                        "p.projectName AS projectName, " +
                        "d.demandName AS demandName, " +
                        "d.status AS status, " +
                        "COALESCE(d.newDueDate, d.dueDate) AS dueDate, " +
                        "d.dueDateChangeCount AS dueDateChangeCount, " +
                        "d.assignDate AS assignDate, " +
                        "d.statusChangeDate AS statusChangeDate, " +
                        "d.priority AS priority, " +
                        "u.fullName AS assignedTo " + // Add assignedTo full name here
                        "FROM Demand d " +
                        "JOIN d.project p " +
                        "LEFT JOIN d.assignedTo u " + // LEFT JOIN to include demands without an assignee
                        "WHERE (:pfId IS NULL OR u.pfId = :pfId) " +
                        "AND (:status IS NULL OR d.status = :status) " +
                        "AND (:projectName IS NULL OR LOWER(p.projectName) LIKE LOWER(CONCAT('%', :projectName, '%'))) "
                        +
                        "AND (:demandName IS NULL OR LOWER(d.demandName) LIKE LOWER(CONCAT('%', :demandName, '%'))) " +
                        "AND (:startDate IS NULL OR d.assignDate >= :startDate) " +
                        "AND (:endDate IS NULL OR d.assignDate <= :endDate) " +
                        "AND (:dueExceeded IS NULL OR " +
                        "     (:dueExceeded = true AND COALESCE(d.newDueDate, d.dueDate) < CURRENT_DATE) OR " +
                        "     (:dueExceeded = false AND COALESCE(d.newDueDate, d.dueDate) >= CURRENT_DATE)) " +
                        "AND (:demandId IS NULL OR d.id = :demandId)")
        Page<SearchDemandResponseDto> searchDemands(
                        @Param("pfId") String pfId,
                        @Param("status") Status status,
                        @Param("projectName") String projectName,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate,
                        @Param("dueExceeded") Boolean dueExceeded,
                        @Param("demandName") String demandName,
                        @Param("demandId") UUID demandId,
                        Pageable pageable);

        // Fetch counts based on multiple criteria in a single query
        @Query("SELECT new com.api.pmtool.dtos.DemandCountResponseDTO(" +
                        "COUNT(d), " +
                        "SUM(CASE WHEN d.status = 'NOT_STARTED' THEN 1 ELSE 0 END), " +
                        "SUM(CASE WHEN d.status = 'IN_PROGRESS' THEN 1 ELSE 0 END), " +
                        "SUM(CASE WHEN d.status = 'COMPLETED' THEN 1 ELSE 0 END), " +
                        "SUM(CASE WHEN d.dueDate BETWEEN :startOfWeek AND :endOfWeek THEN 1 ELSE 0 END), " +
                        "SUM(CASE WHEN d.dueDate < :currentDate THEN 1 ELSE 0 END), " +
                        "COUNT(CASE WHEN d.dueDateChangeCount > 0 THEN d.id ELSE NULL END)) " +
                        "FROM Demand d")
        DemandCountResponseDTO fetchDemandCounts(@Param("startOfWeek") LocalDate startOfWeek,
                        @Param("endOfWeek") LocalDate endOfWeek,
                        @Param("currentDate") LocalDate currentDate);
}
