package com.api.pmtool.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.api.pmtool.dtos.UsersByRoleNameResponseDTO;
import com.api.pmtool.entity.User;
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
 @Query("SELECT u FROM User u JOIN u.roles r WHERE r.id = :roleId")
    List<User> findUsersByRoleId(@Param("roleId") UUID roleId);

//@Query("SELECT u FROM User u JOIN u.roles r WHERE r.roleName = :roleName")
@Query("SELECT u.id AS id, u.fullName AS fullName, u.email AS email FROM User u JOIN u.roles r WHERE r.roleName = :roleName")
List<UsersByRoleNameResponseDTO> findByRoleName(@Param("roleName") String roleName);
}

