package com.example.bookstore.service;

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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private KeycloakAdminService keycloakAdminService;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UUID userId;
    private User userEntity;
    private UserRequest userRequest;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        userEntity = new User();
        userEntity.setUserId(userId);
        userEntity.setUsername("testuser");
        userEntity.setEmail("test@bookstore.com");
        userEntity.setRole(Role.CUSTOMER);
        userEntity.setEnabled(true);

        userRequest = new UserRequest();
        userRequest.setUsername("testuser");
        userRequest.setEmail("test@bookstore.com");
        userRequest.setPassword("password");

        userResponse = new UserResponse();
        userResponse.setUserId(userId);
        userResponse.setUsername("testuser");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockSecurityContextWithRole(String role) {
        Authentication authentication = mock(Authentication.class);
        GrantedAuthority authority = new SimpleGrantedAuthority(role);

        doReturn(Collections.singletonList(authority)).when(authentication).getAuthorities();

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void searchUsers_ShouldReturnPagedUserResponses() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<User> userPage = new PageImpl<>(List.of(userEntity), pageable, 1);

        when(userRepository.searchUsers(eq("test"), eq(Role.CUSTOMER), eq(true), any(Pageable.class)))
                .thenReturn(userPage);
        when(userMapper.toResponse(userEntity)).thenReturn(userResponse);

        Page<UserResponse> result = userService.searchUsers("test", Role.CUSTOMER, true, 0, 20, "username,asc");

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(userId, result.getContent().get(0).getUserId());
    }

    @Test
    void findById_ShouldReturnUser_WhenExists() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(userMapper.toResponse(userEntity)).thenReturn(userResponse);

        UserResponse result = userService.findById(userId);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
    }

    @Test
    void findById_ShouldThrowException_WhenUserDoesNotExist() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.findById(userId));
    }

    @Test
    void save_ShouldThrowException_WhenEmailExists() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> userService.save(userRequest));
        verify(keycloakAdminService, never()).createUser(any(), any(), any(), any());
    }

    @Test
    void save_ShouldThrowException_WhenUsernameExists() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> userService.save(userRequest));
        verify(keycloakAdminService, never()).createUser(any(), any(), any(), any());
    }

    @Test
    void save_ShouldCreateUserSuccessfully() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(keycloakAdminService.createUser(any(), any(), any(), any())).thenReturn(userId);
        when(userMapper.toEntity(any())).thenReturn(userEntity);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.saveAndFlush(any())).thenReturn(userEntity);
        when(userMapper.toResponse(any())).thenReturn(userResponse);

        UserResponse result = userService.save(userRequest);

        assertNotNull(result);
        verify(userRepository).saveAndFlush(userEntity);
        verify(keycloakAdminService, never()).deleteUser(any());
    }

    @Test
    void save_ShouldRollbackKeycloak_WhenDatabaseFails() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(keycloakAdminService.createUser(any(), any(), any(), any())).thenReturn(userId);
        when(userMapper.toEntity(any())).thenReturn(userEntity);

        when(userRepository.saveAndFlush(any())).thenThrow(new RuntimeException("Database down!"));

        assertThrows(RuntimeException.class, () -> userService.save(userRequest));
        verify(keycloakAdminService, times(1)).deleteUser(userId);
    }

    @Test
    void update_ShouldRevertRole_WhenUserIsNotAdmin() {
        mockSecurityContextWithRole("ROLE_CUSTOMER");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));

        User mappedEntity = new User();
        mappedEntity.setRole(Role.ADMIN);
        when(userMapper.toEntity(userRequest)).thenReturn(mappedEntity);

        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any())).thenReturn(mappedEntity);
        when(userMapper.toResponse(any())).thenReturn(userResponse);

        userService.update(userRequest, userId);

        assertEquals(Role.CUSTOMER, mappedEntity.getRole());
        verify(userRepository).save(mappedEntity);
    }

    @Test
    void update_ShouldAllowRoleChange_WhenUserIsAdmin() {
        mockSecurityContextWithRole("ROLE_ADMIN");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));

        User mappedEntity = new User();
        mappedEntity.setRole(Role.ADMIN);
        when(userMapper.toEntity(userRequest)).thenReturn(mappedEntity);

        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any())).thenReturn(mappedEntity);
        when(userMapper.toResponse(any())).thenReturn(userResponse);

        userService.update(userRequest, userId);

        assertEquals(Role.ADMIN, mappedEntity.getRole());
    }

    @Test
    void patch_ShouldUpdateFieldsCorrectly() {
        mockSecurityContextWithRole("ROLE_CUSTOMER");

        UserPatchRequest patchRequest = new UserPatchRequest();

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(userRepository.save(userEntity)).thenReturn(userEntity);
        when(userMapper.toResponse(userEntity)).thenReturn(userResponse);

        UserResponse result = userService.patch(userId, patchRequest);

        assertNotNull(result);
        verify(userMapper).patchEntityFromRequest(patchRequest, userEntity);
        verify(userRepository).save(userEntity);
    }

    @Test
    void delete_ShouldDeleteFromDatabaseAndKeycloak() {
        when(userRepository.existsById(userId)).thenReturn(true);

        userService.delete(userId);

        verify(userRepository).deleteById(userId);
        verify(keycloakAdminService).deleteUser(userId);
    }

    @Test
    void delete_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> userService.delete(userId));

        verify(userRepository, never()).deleteById(any());
        verify(keycloakAdminService, never()).deleteUser(any());
    }
}