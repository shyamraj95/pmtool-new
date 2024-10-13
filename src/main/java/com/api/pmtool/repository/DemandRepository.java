package com.api.pmtool.repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.api.pmtool.entity.Demand;

@Repository
public interface DemandRepository extends JpaRepository<Demand, UUID> {
    @Query("SELECT DISTINCT d FROM Demand d LEFT JOIN FETCH d.comments WHERE d.id = :demandId")
    Optional<Demand> findByIdWithCommentsAndUploads(@Param("demandId") UUID demandId);

    @EntityGraph(attributePaths = {"userRoles", "comments", "uploads"}) // Eager fetch the userRoles collection
    @Query("SELECT d FROM Demand d " +
    "LEFT JOIN d.userRoles ur " +
    "WHERE (:userId IS NULL OR KEY(ur).id = :userId) " +
    "AND (:userRole IS NULL OR ur = :userRole) " +
    "AND (:projectName IS NULL OR LOWER(d.projectName) LIKE LOWER(CONCAT('%', :projectName, '%')))")
Page<Demand> searchDemands(@Param("userId") UUID userId,
                        @Param("userRole") String userRole,
                        @Param("projectName") String projectName,
                        Pageable pageable);

    // Query to filter demands by assignee, tech lead, or department
/*     @Query("SELECT d FROM Demand d WHERE (d.assignee.department = :department OR d.techLead.department = :department) " +
           "OR (d.assignee = :assignee OR d.techLead = :techLead)")
    Page<Demand> findByAssigneeOrTechLeadOrDepartment(@Param("assignee") User assignee, 
                                                      @Param("techLead") User techLead, 
                                                      @Param("department") String department, 
                                             ̰          Pageable pageable);

    Page<Demand> findByDueDateBeforeAndStatusNot(LocalDate dueDate, Status status, Pageable pageable); */
}

