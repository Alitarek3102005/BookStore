package com.example.bookstore.service;

import com.example.bookstore.domain.Cart;
import com.example.bookstore.domain.Role;
import com.example.bookstore.domain.User;
import com.example.bookstore.dto.UserPatchRequest;
import com.example.bookstore.dto.UserRequest;
import com.example.bookstore.dto.UserResponse;
import com.example.bookstore.exception.DuplicateResourceException;
import com.example.bookstore.exception.UserNotFoundException;
import com.example.bookstore.mapper.UserMapper;
import com.example.bookstore.repository.CartRepository;
import com.example.bookstore.repository.UserRepository;
import com.example.bookstore.security.keycloak.KeycloakAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final CartRepository cartRepository;
    private final KeycloakAdminService keycloakAdminService;
    private final PasswordEncoder passwordEncoder;

    private Pageable createPageable(Integer page, Integer size, String sort) {
        Sort sortObj = Sort.unsorted();
        if (sort != null && sort.contains(",")) {
            String[] sortParams = sort.split(",");
            sortObj = Sort.by(Sort.Direction.fromString(sortParams[1]), sortParams[0]);
        } else if (sort != null) {
            sortObj = Sort.by(Sort.Direction.ASC, sort);
        }
        return PageRequest.of(page != null ? page : 0, size != null ? size : 20, sortObj);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> searchUsers(String keyword, Role role, Boolean enabled, Integer page, Integer size, String sort) {
        Pageable pageable = createPageable(page, size, sort);
        return userRepository.searchUsers(keyword, role, enabled, pageable)
                .map(userMapper::toResponse);
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

            User savedUser = userRepository.saveAndFlush(userEntity);

            Cart cart = new Cart();
            cart.setCustomer(savedUser);
            cartRepository.save(cart);

            return userMapper.toResponse(savedUser);

        } catch (RuntimeException ex) {
            keycloakAdminService.deleteUser(keycloakUserId);
            throw ex;
        }
    }

    @Transactional
    public UserResponse update(UserRequest request, UUID id) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + id));
        if (!existing.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email is already registered.");
        }
        if (!existing.getUsername().equals(request.getUsername()) && userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username is already taken.");
        }
        Role previousRole = existing.getRole();
        boolean previousEnabled = existing.isEnabled();
        String previousPassword = existing.getPassword();
        User userEntity = userMapper.toEntity(request);
        userEntity.setUserId(id);

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            userEntity.setPassword(passwordEncoder.encode(request.getPassword()));
            keycloakAdminService.updatePassword(id, request.getPassword());
        } else {
            userEntity.setPassword(previousPassword);
        }

        if (!isAdmin()) {
            userEntity.setRole(previousRole);
            userEntity.setEnabled(previousEnabled);
        } else {
            if (previousRole != userEntity.getRole()) {
                keycloakAdminService.updateUserRole(id, userEntity.getRole().name());
            }
        }

        keycloakAdminService.updateUser(id, request.getUsername(), request.getEmail(), userEntity.isEnabled());

        return userMapper.toResponse(userRepository.save(userEntity));
    }

    @Transactional
    public UserResponse patch(UUID id, UserPatchRequest userPatchRequest) {
        User userEntity = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + id));

        Role previousRole = userEntity.getRole();
        boolean previousEnabled = userEntity.isEnabled();

        userMapper.patchEntityFromRequest(userPatchRequest, userEntity);

        userRepository.findByEmail(userEntity.getEmail()).ifPresent(foundUser -> {
            if (!foundUser.getUserId().equals(id)) {
                throw new DuplicateResourceException("Email is already registered.");
            }
        });

        userRepository.findByUsername(userEntity.getUsername()).ifPresent(foundUser -> {
            if (!foundUser.getUserId().equals(id)) {
                throw new DuplicateResourceException("Username is already taken.");
            }
        });

        if (userPatchRequest.getPassword() != null && !userPatchRequest.getPassword().isEmpty()) {
            userEntity.setPassword(passwordEncoder.encode(userPatchRequest.getPassword()));
            keycloakAdminService.updatePassword(id, userPatchRequest.getPassword());
        }

        if (!isAdmin()) {
            userEntity.setRole(previousRole);
            userEntity.setEnabled(previousEnabled);
        } else {
            if (previousRole != userEntity.getRole()) {
                keycloakAdminService.updateUserRole(id, userEntity.getRole().name());
            }
        }

        keycloakAdminService.updateUser(id, userEntity.getUsername(), userEntity.getEmail(), userEntity.isEnabled());

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