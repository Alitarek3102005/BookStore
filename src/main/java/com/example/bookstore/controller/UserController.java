package com.example.bookstore.controller;

import com.example.bookstore.api.UsersApi;
import com.example.bookstore.dto.UserPatchRequest;
import com.example.bookstore.dto.UserRequest;
import com.example.bookstore.dto.UserResponse;
import com.example.bookstore.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class UserController implements UsersApi {

    private final UserService userService;

    @Override
    public ResponseEntity<UserResponse> createUser(UserRequest userRequest) {
        return new ResponseEntity<>(userService.save(userRequest), HttpStatus.CREATED);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or @ownershipSecurity.isSelf(authentication, #userId)")
    public ResponseEntity<Void> deleteUser(UUID userId) {
        userService.delete(userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.findAll());
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or @ownershipSecurity.isSelf(authentication, #userId)")
    public ResponseEntity<UserResponse> getUserById(UUID userId) {
        return new ResponseEntity<>(userService.findById(userId), HttpStatus.OK);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or @ownershipSecurity.isSelf(authentication, #userId)")
    public ResponseEntity<UserResponse> patchUser(UUID userId, UserPatchRequest userPatchRequest) {
        return new ResponseEntity<>(userService.patch(userId, userPatchRequest), HttpStatus.OK);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or @ownershipSecurity.isSelf(authentication, #userId)")
    public ResponseEntity<UserResponse> updateUser(UUID userId, UserRequest userRequest) {
        return new ResponseEntity<>(userService.update(userRequest, userId), HttpStatus.OK);
    }
}