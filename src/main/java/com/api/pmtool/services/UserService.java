package com.api.pmtool.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.HashSet;
import java.util.Set;
import com.api.pmtool.dtos.UserDto;
import com.api.pmtool.entity.Role;
import com.api.pmtool.entity.User;
import com.api.pmtool.repository.RoleRepository;
import com.api.pmtool.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    // Create a new User
     @Transactional
    public User createUser(UserDto userDto) {
        User user = new User();
        user.setFullName(userDto.getFullName());
        user.setMobileNumber(userDto.getMobileNumber());
        user.setEmail(userDto.getEmail());
        user.setGender(userDto.getGender());
        user.setDesignation(userDto.getDesignation());
        user.setDepartment(userDto.getDepartment());
        user.setStatus(userDto.getStatus());

        // Fetch and assign roles
        Set<Role> roles = new HashSet<>();
        for (UUID roleId : userDto.getRoleIds()) {
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new IllegalArgumentException("Role not found"));
            roles.add(role);
        }
        user.setRoles(roles);

        return userRepository.save(user);
    }

    // Get all Users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Get a User by ID
    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));
    }

    // Update an existing User
    @Transactional
    public User updateUser(UUID id, UserDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));

        user.setFullName(userDto.getFullName());
        user.setMobileNumber(userDto.getMobileNumber());
        user.setEmail(userDto.getEmail());
        user.setGender(userDto.getGender());
        user.setDesignation(userDto.getDesignation());
        user.setDepartment(userDto.getDepartment());
        user.setStatus(userDto.getStatus());

        // Fetch and assign roles
        Set<Role> roles = new HashSet<>();
        for (UUID roleId : userDto.getRoleIds()) {
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new IllegalArgumentException("Role not found"));
            roles.add(role);
        }
        user.setRoles(roles);

        return userRepository.save(user);
    }

    // Delete a User by ID
    @Transactional
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));
        userRepository.delete(user);
    }
}
