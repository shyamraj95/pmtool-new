package com.api.pmtool.controllers;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.pmtool.dtos.RoleDto;

import com.api.pmtool.entity.Role;
import com.api.pmtool.services.RolesService;


@RestController
@RequestMapping("/api/v1/role")
public class RoleController {

    @Autowired
    private RolesService rolesService;

    // Create Role
    @PostMapping(consumes = { MediaType.APPLICATION_JSON_VALUE }) 
    public ResponseEntity<Role> createRole(@RequestBody @Valid RoleDto roleDto) {
        Role createdRole = rolesService.createRole(roleDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRole);
    }

    // Get all Roles
    @GetMapping()
    public ResponseEntity<List<Role>> getAllRoles() {
        List<Role> Roles = rolesService.getAllRoles();
        return ResponseEntity.ok(Roles);
    }

    // Get Role by ID
    @GetMapping("/{id}")
    public ResponseEntity<Role> getRoleById(@PathVariable UUID id) {
        Role Role = rolesService.getRoleById(id);
        return ResponseEntity.ok(Role);
    }

    // Update Role
    @PutMapping("/{id}")
    public ResponseEntity<Role> updateRole(@PathVariable UUID id, @RequestBody @Valid RoleDto roleDto) {
        Role updatedRole = rolesService.updateRole(id, roleDto);
        return ResponseEntity.ok(updatedRole);
    }

    // Delete Role
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable UUID id) {
        rolesService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }
}
