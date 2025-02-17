package com.api.pmtool.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.api.pmtool.dtos.TaskStatisticsCountDTO;
import com.api.pmtool.dtos.UsersByRoleNameResponseDTO;
import com.api.pmtool.entity.User;
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
 @Query("SELECT u FROM User u JOIN u.roles r WHERE r.id = :roleId")
    List<User> findUsersByRoleId(@Param("roleId") UUID roleId);

//@Query("SELECT u FROM User u JOIN u.roles r WHERE r.roleName = :roleName")
@Query("SELECT u.id AS id, u.fullName AS fullName, u.email AS email FROM User u JOIN u.roles r WHERE r.roleName = :roleName")
List<UsersByRoleNameResponseDTO> findByRoleName(@Param("roleName") String roleName);

@Query("SELECT DISTINCT u FROM User u " +
       "LEFT JOIN u.tasks t " +
       "WHERE (:criteria = 'developersWithoutTasks' AND (t.id IS NULL OR t.workProgress NOT IN ('COMPLETED', 'ON_HOLD', 'CANCELED'))) " +
       "OR (:criteria = 'developersWithMultipleTasks' AND SIZE(u.tasks) > 2 AND NOT EXISTS " +
       "(SELECT t FROM u.tasks t WHERE t.workProgress IN ('COMPLETED', 'ON_HOLD', 'CANCELED'))) " +
       "OR (:criteria = 'tasksPendingOneWeek' AND t.dueDate < CURRENT_DATE - 7 AND t.workProgress NOT IN ('COMPLETED', 'ON_HOLD', 'CANCELED')) " +
       "OR (:criteria = 'tasksPendingTwoWeeks' AND t.dueDate < CURRENT_DATE - 14 AND t.workProgress NOT IN ('COMPLETED', 'ON_HOLD', 'CANCELED')) " +
       "OR (:criteria = 'tasksPendingThreeOrMoreWeeks' AND t.dueDate < CURRENT_DATE - 21 AND t.workProgress NOT IN ('COMPLETED', 'ON_HOLD', 'CANCELED')) " +
       "OR (:criteria = 'overdueTasks' AND t.dueDate < CURRENT_DATE AND t.workProgress NOT IN ('COMPLETED', 'ON_HOLD', 'CANCELED')) " +
       "OR (:criteria = 'developersNotUpdatingProgress' AND t.developmentInPercentage = 0)")
List<User> findUsersByTaskStatistic(@Param("criteria") String criteria);



@Query("SELECT new com.api.pmtool.dtos.TaskStatisticsCountDTO( " +
       "COUNT(DISTINCT CASE WHEN t.id IS NULL OR t.workProgress NOT IN ('COMPLETED', 'ON_HOLD', 'CANCELED') THEN u END), " +
       "COUNT(DISTINCT CASE WHEN SIZE(u.tasks) > 2 AND NOT EXISTS " +
       "(SELECT t FROM u.tasks t WHERE t.workProgress IN ('COMPLETED', 'ON_HOLD', 'CANCELED')) THEN u END), " +
       "COUNT(DISTINCT CASE WHEN t.dueDate < CURRENT_DATE - 7 AND t.workProgress NOT IN ('COMPLETED', 'ON_HOLD', 'CANCELED') THEN u END), " +
       "COUNT(DISTINCT CASE WHEN t.dueDate < CURRENT_DATE - 14 AND t.workProgress NOT IN ('COMPLETED', 'ON_HOLD', 'CANCELED') THEN u END), " +
       "COUNT(DISTINCT CASE WHEN t.dueDate < CURRENT_DATE - 21 AND t.workProgress NOT IN ('COMPLETED', 'ON_HOLD', 'CANCELED') THEN u END), " +
       "COUNT(DISTINCT CASE WHEN t.dueDate < CURRENT_DATE AND t.workProgress NOT IN ('COMPLETED', 'ON_HOLD', 'CANCELED') THEN u END), " +
       "COUNT(DISTINCT CASE WHEN t.developmentInPercentage = 0 THEN u END) ) " +
       "FROM User u LEFT JOIN u.tasks t")
TaskStatisticsCountDTO getTaskStatisticsCounts();


/* @Query("SELECT new com.api.pmtool.dtos.TaskStatisticsCountDTO( " +
       "COUNT(DISTINCT CASE WHEN t.id IS NULL OR t.workProgress NOT IN ('COMPLETED', 'ON_HOLD', 'CANCELED') THEN u END), " +
       "COUNT(DISTINCT CASE WHEN SIZE(u.tasks) > 2 AND NOT EXISTS " +
       "(SELECT t FROM u.tasks t WHERE t.workProgress IN ('COMPLETED', 'ON_HOLD', 'CANCELED')) THEN u END), " +
       "COUNT(DISTINCT CASE WHEN t.assignDate < CURRENT_DATE - 7 AND t.workProgress NOT IN ('COMPLETED', 'ON_HOLD', 'CANCELED') THEN u END), " +  // Changed from dueDate to assignDate
       "COUNT(DISTINCT CASE WHEN t.assignDate < CURRENT_DATE - 14 AND t.workProgress NOT IN ('COMPLETED', 'ON_HOLD', 'CANCELED') THEN u END), " + // Changed from dueDate to assignDate
       "COUNT(DISTINCT CASE WHEN t.assignDate < CURRENT_DATE - 21 AND t.workProgress NOT IN ('COMPLETED', 'ON_HOLD', 'CANCELED') THEN u END), " + // Changed from dueDate to assignDate
       "COUNT(DISTINCT CASE WHEN t.dueDate < CURRENT_DATE AND t.workProgress NOT IN ('COMPLETED', 'ON_HOLD', 'CANCELED') THEN u END), " +
       "COUNT(DISTINCT CASE WHEN t.developmentInPercentage = 0 THEN u END) ) " +
       "FROM User u LEFT JOIN u.tasks t")
TaskStatisticsCountDTO getTaskStatisticsCounts(); */

}

