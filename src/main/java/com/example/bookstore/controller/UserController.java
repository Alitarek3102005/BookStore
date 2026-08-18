package com.example.bookstore.controller;
import com.example.bookstore.api.UsersApi;
import com.example.bookstore.dto.UserPatchRequest;
import com.example.bookstore.dto.UserRequest;
import com.example.bookstore.dto.UserResponse;
import com.example.bookstore.service.UserServcie;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.UUID;
@RestController
@RequiredArgsConstructor
public class UserController implements UsersApi{
    private final UserServcie userServcie;

    @Override
    public ResponseEntity<UserResponse> createUser(UserRequest userRequest) {
        return new ResponseEntity<>(userServcie.save(userRequest),HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Void> deleteUser(UUID userId) {
        userServcie.delete(userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userServcie.findAll());
    }

    @Override
    public ResponseEntity<UserResponse> getUserById(UUID userId) {
        return new ResponseEntity<>(userServcie.findById(userId),HttpStatus.OK);
    }

    @Override
    public ResponseEntity<UserResponse> patchUser(UUID userId, UserPatchRequest userPatchRequest) {
        return new ResponseEntity<>(userServcie.patch(userId,userPatchRequest),HttpStatus.OK);
    }

    @Override
    public ResponseEntity<UserResponse> updateUser(UUID userId, UserRequest userRequest) {
        return new ResponseEntity<>(userServcie.update(userRequest,userId),HttpStatus.OK);
    }

}
