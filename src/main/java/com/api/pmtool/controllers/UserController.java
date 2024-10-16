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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.api.pmtool.dtos.UserDto;
import com.api.pmtool.dtos.UsersByRoleNameResponseDTO;
import com.api.pmtool.entity.User;
import com.api.pmtool.enums.UserStatus;
import com.api.pmtool.services.UserService;


@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    @Autowired
    private UserService userService;

    // Create User
    @PostMapping(consumes = { MediaType.APPLICATION_JSON_VALUE }) 
    public ResponseEntity<User> createUser(@RequestBody @Valid UserDto userDto) {
        User createdUser = userService.createUser(userDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    // Get all Users
    @GetMapping()
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> Users = userService.getAllUsers();
        return ResponseEntity.ok(Users);
    }
    @GetMapping("/role/{roleId}")
    public ResponseEntity<List<User>> getUsersByRoleId(@PathVariable UUID roleId) {
        List<User> users = userService.getUsersByRoleId(roleId);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/list/role/{roleName}")
    public ResponseEntity<List<UsersByRoleNameResponseDTO>> getUsersByRoleName(@PathVariable String roleName) {
        List<UsersByRoleNameResponseDTO> users = userService.getUsersByRoleName(roleName);
        if (users.isEmpty()) {
            return ResponseEntity.noContent().build();  // Return 204 if no users are found
        }
        return ResponseEntity.ok(users);  // Return the list of users with the given role name
    }
    @PutMapping("/{userId}/status")
    public ResponseEntity<User> changeUserStatus(@PathVariable UUID userId, @RequestParam UserStatus newStatus) {
        User updatedUser = userService.changeUserStatus(userId, newStatus);
        return ResponseEntity.ok(updatedUser);
    }
    // Get User by ID
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable UUID id) {
        User User = userService.getUserById(id);
        return ResponseEntity.ok(User);
    }

    // Update User
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable UUID id, @RequestBody @Valid UserDto userDto) {
        User updatedUser = userService.updateUser(id, userDto);
        return ResponseEntity.ok(updatedUser);
    }

    // Delete User
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
