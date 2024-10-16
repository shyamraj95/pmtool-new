package com.api.pmtool.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.HashSet;
import java.util.Set;
import com.api.pmtool.dtos.UserDto;
import com.api.pmtool.dtos.UsersByRoleNameResponseDTO;
import com.api.pmtool.entity.Role;
import com.api.pmtool.entity.User;
import com.api.pmtool.enums.UserStatus;
import com.api.pmtool.repository.RoleRepository;
import com.api.pmtool.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

/**
 * Creates a new user based on the provided UserDto.
 * 
 * This method initializes a User entity with data from the given UserDto
 * and assigns the appropriate roles. The user is then saved to the repository.
 * 
 * @param userDto The User Data Transfer Object containing user details.
 * @return The persisted User entity.
 * @throws IllegalArgumentException if any role is not found during assignment.
 */
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

    /**
     * Retrieves all users.
     * @return A list of all users.
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Retrieves a user by their ID.
     * @param id Unique identifier for the user.
     * @return The user with the given ID or throws an exception if no user is found.
     */
    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));
    }


    /**
     * Updates a user.
     * @param id Unique identifier for the user.
     * @param userDto The user data to update.
     * @return The updated user.
     */
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

    /**
     * Deletes a user by its ID.
     * @param id Unique identifier for the user to delete.
     * @throws IllegalArgumentException if no user is found with the given ID.
     */
    @Transactional
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));
        userRepository.delete(user);
    }
    
    /**
     * Retrieve a list of Users by Role ID.
     * @param roleId Unique identifier for the Role.
     * @return A list of Users associated with the Role.
     */
    public List<User> getUsersByRoleId(UUID roleId) {
        return userRepository.findUsersByRoleId(roleId);
    }
    /**
     * Retrieves a list of users by role name.
     * @param roleName The role name to search for.
     * @return A list of users associated with the role name.
     */
    public List<UsersByRoleNameResponseDTO> getUsersByRoleName(String roleName) {
        return userRepository.findByRoleName(roleName);
    }
    /**
     * Changes the status of a user.
     * @param userId Unique identifier for the user.
     * @param newStatus The new status to set for the user.
     * @return The updated user with the new status set.
     */
    public User changeUserStatus(UUID userId, UserStatus newStatus) {
        // Fetch user by ID
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        // Update the user status
        user.setStatus(newStatus);

        // Save and return updated user
        return userRepository.save(user);
    }
}
