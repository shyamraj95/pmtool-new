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

    // Create a new Developer
     @Transactional
    public Role createRole(RoleDto roleDto) {
        Role role = new Role();
        role.setRoleName(roleDto.getRoleName());
        return roleRepository.save(role);
    }

    // Get all Developers
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    // Get Developer by ID
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

    // Delete Developer
    @Transactional
    public void deleteRole(UUID id) {
        Role role = roleRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Developer not found with ID: " + id));
            roleRepository.delete(role);
    }
}
