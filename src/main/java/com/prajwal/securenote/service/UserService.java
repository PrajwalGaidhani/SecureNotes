package com.prajwal.securenote.service;



import com.prajwal.securenote.dtos.UserDTO;
import com.prajwal.securenote.models.Role;
import com.prajwal.securenote.models.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    void updateUserRole(Long userId, String roleName);

    List<User> getAllUsers();

    UserDTO getUserById(Long id);

    User findByUsername(String username);

    void updateAccountLockStatus(Long userId, boolean lock);

    List<Role> getAllRoles();

    void updateAccountExpiryStatus(Long userId, boolean expire);

    void updateAccountEnabledStatus(Long userId, boolean enabled);

    void updateCredentialsExpiryStatus(Long userId, boolean expire);

    void updatePassword(Long userId, String password);








}