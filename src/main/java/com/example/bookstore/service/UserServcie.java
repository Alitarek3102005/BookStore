package com.example.bookstore.service;

import com.example.bookstore.domain.User;
import com.example.bookstore.dto.UserPatchRequest;
import com.example.bookstore.dto.UserRequest;
import com.example.bookstore.dto.UserResponse;
import com.example.bookstore.exception.customExceptions.UserNotFoundException;
import com.example.bookstore.mapper.UserMapper;
import com.example.bookstore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor

public class UserServcie {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public List<UserResponse> findAll() {
        Iterable<User> users = userRepository.findAll();
        List<UserResponse> usersResponse = new ArrayList<>();

        for (User user : users) {
            usersResponse.add(userMapper.toResponse(user));
        }

        return usersResponse;
}
public UserResponse findById(UUID id){
        User user = userRepository.findById(id).orElse(null);
        if (user != null){
            return userMapper.toResponse(user);
        }else {
            throw new UserNotFoundException("User not found");
        }
    }
    public UserResponse save(UserRequest user){
        User userEntity = userMapper.toEntity(user);
        userRepository.save(userEntity);
        return userMapper.toResponse(userEntity);
    }
    public UserResponse update(UserRequest user, UUID id) {
        User userEntity = userRepository.findById(id).orElse(null);

        if (userEntity != null) {
            userEntity = userMapper.toEntity(user);
            userRepository.save(userEntity);

            return userMapper.toResponse(userEntity);
        } else {
            throw new UserNotFoundException("User not found");
        }
   }
public void delete(UUID id){
    userRepository.deleteById(id);
    }
    public UserResponse patch(UUID id, UserPatchRequest userPatchRequest) {
        User userEntity = userRepository.findById(id).orElse(null);

        if (userEntity != null) {
            userMapper.patchEntityFromRequest(
                    userPatchRequest,
                    userEntity
            );

            userRepository.save(userEntity);

            return userMapper.toResponse(userEntity);
        } else {
            throw new UserNotFoundException("User not found");
        }
    }
}