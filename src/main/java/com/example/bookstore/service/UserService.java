package com.example.bookstore.service;

import com.example.bookstore.domain.Role;
import com.example.bookstore.domain.User;
import com.example.bookstore.dto.UserPatchRequest;
import com.example.bookstore.dto.UserRequest;
import com.example.bookstore.dto.UserResponse;
import com.example.bookstore.exception.UserNotFoundException;
import com.example.bookstore.mapper.UserMapper;
import com.example.bookstore.repository.UserRepository;
import com.example.bookstore.security.keycloak.KeycloakAdminService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final KeycloakAdminService keycloakAdminService;

    public List<UserResponse> findAll() {
        Iterable<User> users = userRepository.findAll();
        List<UserResponse> usersResponse = new ArrayList<>();

        for (User user : users) {
            usersResponse.add(userMapper.toResponse(user));
        }

        return usersResponse;
    }

    public UserResponse findById(UUID id) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            return userMapper.toResponse(user);
        } else {
            throw new UserNotFoundException("User not found");
        }
    }

    @Transactional
    public UserResponse save(UserRequest request) {
        Role registrationRole = Role.CUSTOMER;

        UUID keycloakUserId = keycloakAdminService.createUser(
                request.getUsername(), request.getEmail(), request.getPassword(), registrationRole.name());

        try {
            User userEntity = userMapper.toEntity(request);
            userEntity.setUserId(keycloakUserId);
            userEntity.setRole(registrationRole);
            userRepository.save(userEntity);
            return userMapper.toResponse(userEntity);
        } catch (RuntimeException ex) {
            keycloakAdminService.deleteUser(keycloakUserId);
            throw ex;
        }
    }

    public UserResponse update(UserRequest request, UUID id) {
        User existing = userRepository.findById(id).orElse(null);

        if (existing == null) {
            throw new UserNotFoundException("User not found");
        }

        Role previousRole = existing.getRole();
        boolean previousEnabled = existing.isEnabled();

        User userEntity = userMapper.toEntity(request);
        userEntity.setUserId(id);

        if (!isAdmin()) {
            userEntity.setRole(previousRole);
            userEntity.setEnabled(previousEnabled);
        }

        userRepository.save(userEntity);
        return userMapper.toResponse(userEntity);
    }

    @Transactional
    public void delete(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found");
        }
        userRepository.deleteById(id);
        keycloakAdminService.deleteUser(id);
    }

    public UserResponse patch(UUID id, UserPatchRequest userPatchRequest) {
        User userEntity = userRepository.findById(id).orElse(null);

        if (userEntity == null) {
            throw new UserNotFoundException("User not found");
        }

        Role previousRole = userEntity.getRole();
        boolean previousEnabled = userEntity.isEnabled();

        userMapper.patchEntityFromRequest(userPatchRequest, userEntity);

        if (!isAdmin()) {
            userEntity.setRole(previousRole);
            userEntity.setEnabled(previousEnabled);
        }

        userRepository.save(userEntity);
        return userMapper.toResponse(userEntity);
    }

    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}