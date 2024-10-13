package com.api.pmtool.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.api.pmtool.entity.Role;
@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
/*     Optional<Role> findByRoleName(String roleName); */
}

