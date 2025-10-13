package com.prajwal.securenote.service;



import com.prajwal.securenote.dtos.UserDTO;
import com.prajwal.securenote.models.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    void updateUserRole(Long userId, String roleName);

    List<User> getAllUsers();

    UserDTO getUserById(Long id);

}