package com.pthieu.identity_service.service;

import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.pthieu.identity_service.dto.request.UserCreationRequest;
import com.pthieu.identity_service.dto.request.UserUpdateRequest;
import com.pthieu.identity_service.dto.response.UserResponse;
import com.pthieu.identity_service.entity.User;
import com.pthieu.identity_service.enums.Role;
import com.pthieu.identity_service.exception.AppException;
import com.pthieu.identity_service.exception.ErrorCode;
import com.pthieu.identity_service.mapper.UserMapper;
import com.pthieu.identity_service.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;

    public UserResponse createUser(UserCreationRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTS);
        }

        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        HashSet<String> roles = new HashSet<>();
        roles.add(Role.USER.name());
        user.setRoles(roles);

        return userMapper.toUserResponse(userRepository.save(user));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toUserResponse)
                .toList();
    }

    @PostAuthorize("hasRole('ADMIN') or returnObject.username == authentication.name")
    public UserResponse getUserByUsername(String username) {
        return userMapper.toUserResponse(userRepository.findAll().stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username)));
    }
    
    public User getUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    }

    public UserResponse updateUserById(String userId, UserUpdateRequest request) {
        User user = getUserById(userId);

        userMapper.updateUserFromRequest(user, request);

        return userMapper.toUserResponse(userRepository.save(user));
    }

    public UserResponse getMyInfo() {
        var content =SecurityContextHolder.getContext();
        String username = content.getAuthentication().getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException(ErrorCode.USER_NOT_FOUND.getMessage()));
        return userMapper.toUserResponse(user);
    }

    public void deleteUserById(String userId) {
        userRepository.deleteById(userId);
        if (userRepository.existsById(userId)) {
            throw new RuntimeException("Failed to delete user with id: " + userId);
        }
    }
}
