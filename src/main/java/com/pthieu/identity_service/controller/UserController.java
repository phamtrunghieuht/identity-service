package com.pthieu.identity_service.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import com.nimbusds.jose.proc.SecurityContext;
import com.pthieu.identity_service.dto.request.ApiResponse;
import com.pthieu.identity_service.dto.request.UserCreationRequest;
import com.pthieu.identity_service.dto.request.UserUpdateRequest;
import com.pthieu.identity_service.dto.response.UserResponse;
import com.pthieu.identity_service.entity.User;
import com.pthieu.identity_service.exception.AppException;
import com.pthieu.identity_service.exception.ErrorCode;
import com.pthieu.identity_service.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PutMapping;
import lombok.AccessLevel;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserController {
    UserService userService;

    @PostMapping
    ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreationRequest request) {
        ApiResponse<UserResponse> response = new ApiResponse<>();
        response.setCode(1000);
        response.setResult(userService.createUser(request));
        return response;
    }

    @GetMapping
    ApiResponse<List<UserResponse>> getAllUsers() {
        var authenticate = SecurityContextHolder.getContext().getAuthentication();
        if (authenticate == null || !authenticate.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        log.info("Username {}",authenticate.getName());
        authenticate.getAuthorities().forEach(grantAuthority -> {
            log.info("Authority: {}", grantAuthority.getAuthority());
        });

        return ApiResponse.<List<UserResponse>>builder()
                .result(userService.getAllUsers())
                .build();
    }

    @GetMapping("/{username}")
    ApiResponse<UserResponse> getUserByUsername(@PathVariable String username) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getUserByUsername(username))
                .build();
    }
    
    @PutMapping("/{userId}")
    ApiResponse<UserResponse> updateUserById(@PathVariable String userId, @RequestBody @Valid UserUpdateRequest entity) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.updateUserById(userId, entity))
                .build();
    }

    @DeleteMapping("/{userId}")
    ApiResponse<String> deleteUserById(@PathVariable String userId) {
        userService.deleteUserById(userId);
        return ApiResponse.<String>builder()
                .result("User deleted successfully")
                .build();
    }

    @GetMapping("/myInfo")
    ApiResponse<UserResponse> getMyInfo() {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getMyInfo())
                .build();
    }
    
}
