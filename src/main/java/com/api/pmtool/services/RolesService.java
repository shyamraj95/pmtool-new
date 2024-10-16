package com.api.pmtool.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.pmtool.dtos.RoleDto;
import com.api.pmtool.entity.Role;
import com.api.pmtool.repository.RoleRepository;

import java.util.List;
import java.util.UUID;
@Service
public class RolesService {

    @Autowired
    private RoleRepository roleRepository;

    /**
     * Creates a new role based on the provided RoleDto.
     * 
     * This method initializes a Role entity with data from the given RoleDto
     * and saves it to the repository.
     * 
     * @param roleDto The Role Data Transfer Object containing role details.
     * @return The persisted Role entity.
     */
     @Transactional
    public Role createRole(RoleDto roleDto) {
        Role role = new Role();
        role.setRoleName(roleDto.getRoleName());
        return roleRepository.save(role);
    }

    /**
     * Retrieves all roles.
     * @return A list of all roles.
     */
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    /**
     * Retrieves a role by its ID.
     * @param id Unique identifier for the role to retrieve.
     * @return The role with the given ID or throws an exception if no role is found.
     */
    public Role getRoleById(UUID id) {
        return roleRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Developer not found with ID: " + id));
    }

    // Update Developer
    @Transactional
    public Role updateRole(UUID id, RoleDto roleDto) {
        Role role = roleRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Developer not found with ID: " + id));
            role.setRoleName(roleDto.getRoleName());
        return roleRepository.save(role);
    }

/**
 * Deletes a role by its ID.
 * @param id Unique identifier for the role to delete.
 * @throws IllegalArgumentException if no role is found with the given ID.
 */
    @Transactional
    public void deleteRole(UUID id) {
        Role role = roleRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Developer not found with ID: " + id));
            roleRepository.delete(role);
    }
}
