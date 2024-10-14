package com.api.pmtool.repository;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.api.pmtool.dtos.SearchDemandResponseDto;
import com.api.pmtool.entity.Demand;


@Repository
public interface DemandRepository extends JpaRepository<Demand, UUID> {
    @Query("SELECT DISTINCT d FROM Demand d LEFT JOIN FETCH d.comments WHERE d.id = :demandId")
    Optional<Demand> findByIdWithCommentsAndUploads(@Param("demandId") UUID demandId);

    // @EntityGraph(attributePaths = {"userRoles","comments"}) // Eager fetch the
    @Query("SELECT DISTINCT d.id AS id, d.projectName AS projectName, d.demandName AS demandName, " +
    "d.status AS status, d.dueDate AS dueDate, d.dueDateChangeCount AS dueDateChangeCount " +
    "FROM Demand d " +
    "LEFT JOIN d.userRoles ur " +
    "WHERE (:userId IS NULL OR KEY(ur).id = :userId) " +
    "AND (:role IS NULL OR ur = :role) " +
    "AND (:projectName IS NULL OR d.projectName LIKE CONCAT('%',:projectName,'%')) " +
    "ORDER BY d.projectName ASC")
Page<SearchDemandResponseDto> searchDemands(
     @Param("userId") UUID userId,
     @Param("role") String role,
     @Param("projectName") String projectName,
     Pageable pageable
);
}
