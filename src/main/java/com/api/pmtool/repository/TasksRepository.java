package com.api.pmtool.repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Repository;

import com.api.pmtool.dtos.SearchTasksResponseDto;
import com.api.pmtool.entity.TasksEntity;
import com.api.pmtool.enums.Status;
import com.api.pmtool.enums.TaskType;

@Repository
public interface TasksRepository extends JpaRepository<TasksEntity, UUID> {

    /*
     * @Query("SELECT t FROM TasksEntity t " +
     * "WHERE (:demandId IS NULL OR t.demand.id = :demandId) " +
     * "AND (:status IS NULL OR t.workProgress = :status) " +
     * "AND (:developerId IS NULL OR t.assignedDeveloper.id = :developerId)")
     * Page<TasksEntity> findAllByCriteria(
     * 
     * @Param("demandId") UUID demandId,
     * 
     * @Param("status") Status status,
     * 
     * @Param("developerId") UUID developerId,
     * Pageable pageable);
     */

    @Query("SELECT DISTINCT d FROM TasksEntity d LEFT JOIN FETCH d.comments WHERE d.id = :taskId")
    Optional<TasksEntity> findByIdWithCommentsAndUploads(@Param("taskId") UUID taskId);

    @Query(value = "SELECT t FROM TasksEntity t " +
            "JOIN t.demand d " +
            "LEFT JOIN t.assignedTo u " +
            "WHERE (:pfId IS NULL OR u.pfId = :pfId) " +
            "AND (:status IS NULL OR t.workProgress = :status) " +
            "AND (:taskName IS NULL OR t.taskName LIKE CONCAT('%', :taskName, '%')) " +
            "AND (:taskType IS NULL OR t.taskType = :taskType) " +
            "AND (:startDate IS NULL OR COALESCE(t.newDueDate, t.dueDate) >= :startDate) " +
            "AND (:endDate IS NULL OR COALESCE(t.newDueDate, t.dueDate) <= :endDate) " +
            "AND (:dueExceeded IS NULL OR " +
            "     (:dueExceeded = TRUE AND COALESCE(t.newDueDate, t.dueDate) < CURRENT_DATE) OR " +
            "     (:dueExceeded = FALSE AND COALESCE(t.newDueDate, t.dueDate) >= CURRENT_DATE)) " +
            "AND (:demandId IS NULL OR d.id = :demandId) " +
            "AND (:demandName IS NULL OR d.demandName LIKE CONCAT('%', :demandName, '%')) " +
            "AND (:taskId IS NULL OR t.id = :taskId)")
    Page<TasksEntity> findTasksByCriteria(
            @Param("pfId") String pfId,
            @Param("status") Status status,
            @Param("taskName") String taskName,
            @Param("taskType") TaskType taskType,
            @Param("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Param("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Param("dueExceeded") Boolean dueExceeded,
            @Param("demandId") UUID demandId,
            @Param("demandName") String demandName,
            @Param("taskId") UUID taskId,
            Pageable pageable);

}
