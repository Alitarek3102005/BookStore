package com.example.bookstore.controller;

import com.example.bookstore.dto.UserPatchRequest;
import com.example.bookstore.dto.UserRequest;
import com.example.bookstore.dto.UserResponse;
import com.example.bookstore.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = UserController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                OAuth2ResourceServerAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UserService userService;

    private UUID userId;
    private UserRequest userRequest;
    private UserPatchRequest userPatchRequest;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        userRequest = new UserRequest();
        userRequest.setUsername("testuser");
        userRequest.setEmail("test@bookstore.com");
        userRequest.setPassword("password123");

        userPatchRequest = new UserPatchRequest();
        userPatchRequest.setUsername("updateduser");

        userResponse = new UserResponse();
        userResponse.setUserId(userId);
        userResponse.setUsername("testuser");
        userResponse.setEmail("test@bookstore.com");
    }

    @Test
    void createUser_ShouldReturn201Created() throws Exception {
        when(userService.save(any(UserRequest.class))).thenReturn(userResponse);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.username").value("testuser"));

        verify(userService).save(any(UserRequest.class));
    }

    @Test
    void getAllUsers_ShouldReturn200Ok() throws Exception {
        when(userService.findAll()).thenReturn(List.of(userResponse));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].username").value("testuser"));

        verify(userService).findAll();
    }

    @Test
    void getUserById_ShouldReturn200Ok() throws Exception {
        when(userService.findById(userId)).thenReturn(userResponse);

        mockMvc.perform(get("/api/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.username").value("testuser"));

        verify(userService).findById(userId);
    }

    @Test
    void updateUser_ShouldReturn200Ok() throws Exception {
        when(userService.update(any(UserRequest.class), eq(userId))).thenReturn(userResponse);

        mockMvc.perform(put("/api/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));

        verify(userService).update(any(UserRequest.class), eq(userId));
    }

    @Test
    void patchUser_ShouldReturn200Ok() throws Exception {
        when(userService.patch(eq(userId), any(UserPatchRequest.class))).thenReturn(userResponse);

        mockMvc.perform(patch("/api/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userPatchRequest)))
                .andExpect(status().isOk());

        verify(userService).patch(eq(userId), any(UserPatchRequest.class));
    }

    @Test
    void deleteUser_ShouldReturn204NoContent() throws Exception {
        mockMvc.perform(delete("/api/users/{userId}", userId))
                .andExpect(status().isNoContent());

        verify(userService).delete(userId);
    }
}