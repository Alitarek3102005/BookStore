package com.example.bookstore.service;

import com.example.bookstore.domain.Role;
import com.example.bookstore.domain.User;
import com.example.bookstore.dto.UserPatchRequest;
import com.example.bookstore.dto.UserRequest;
import com.example.bookstore.dto.UserResponse;
import com.example.bookstore.exception.DuplicateResourceException;
import com.example.bookstore.exception.UserNotFoundException;
import com.example.bookstore.mapper.UserMapper;
import com.example.bookstore.repository.UserRepository;
import com.example.bookstore.security.keycloak.KeycloakAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final KeycloakAdminService keycloakAdminService;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse findById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + id));
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse save(UserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email is already registered.");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username is already taken.");
        }
        Role registrationRole = Role.CUSTOMER;

        UUID keycloakUserId = keycloakAdminService.createUser(
                request.getUsername(), request.getEmail(), request.getPassword(), registrationRole.name());
        try {
            User userEntity = userMapper.toEntity(request);
            userEntity.setUserId(keycloakUserId);
            userEntity.setRole(registrationRole);

            userEntity.setPassword(passwordEncoder.encode(request.getPassword()));

            return userMapper.toResponse(userRepository.saveAndFlush(userEntity));
        } catch (RuntimeException ex) {
            keycloakAdminService.deleteUser(keycloakUserId);
            throw ex;
        }
    }

    @Transactional
    public UserResponse update(UserRequest request, UUID id) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + id));

        Role previousRole = existing.getRole();
        boolean previousEnabled = existing.isEnabled();

        User userEntity = userMapper.toEntity(request);
        userEntity.setUserId(id);

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            userEntity.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (!isAdmin()) {
            userEntity.setRole(previousRole);
            userEntity.setEnabled(previousEnabled);
        }

        return userMapper.toResponse(userRepository.save(userEntity));
    }

    @Transactional
    public UserResponse patch(UUID id, UserPatchRequest userPatchRequest) {
        User userEntity = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + id));

        Role previousRole = userEntity.getRole();
        boolean previousEnabled = userEntity.isEnabled();

        userMapper.patchEntityFromRequest(userPatchRequest, userEntity);

        if (!isAdmin()) {
            userEntity.setRole(previousRole);
            userEntity.setEnabled(previousEnabled);
        }

        return userMapper.toResponse(userRepository.save(userEntity));
    }

    @Transactional
    public void delete(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found: " + id);
        }
        userRepository.deleteById(id);
        keycloakAdminService.deleteUser(id);
    }

    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}