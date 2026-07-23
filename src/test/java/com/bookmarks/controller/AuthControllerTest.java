package com.bookmarks.controller;

import com.bookmarks.User;
import com.bookmarks.UserRepository;
import com.bookmarks.dto.LoginRequest;
import com.bookmarks.dto.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setup() {
        userRepository.deleteAll();
    }

    @Test
    public void testRegisterNewUserSuccess() throws Exception {
        RegisterRequest request = new RegisterRequest("newuser", "newuser@example.com", "password123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.user.username", is("newuser")))
                .andExpect(jsonPath("$.user.email", is("newuser@example.com")));

        assertTrue(userRepository.findByUsername("newuser").isPresent());
    }

    @Test
    public void testRegisterUsernameConflict() throws Exception {
        User existingUser = new User();
        existingUser.setUsername("existinguser");
        existingUser.setEmail("existing@example.com");
        existingUser.setPasswordHash(passwordEncoder.encode("password123"));
        userRepository.save(existingUser);

        RegisterRequest request = new RegisterRequest("existinguser", "other@example.com", "password123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", is("Username is already taken")));
    }

    @Test
    public void testRegisterEmailConflict() throws Exception {
        User existingUser = new User();
        existingUser.setUsername("existinguser");
        existingUser.setEmail("existing@example.com");
        existingUser.setPasswordHash(passwordEncoder.encode("password123"));
        userRepository.save(existingUser);

        RegisterRequest request = new RegisterRequest("otheruser", "existing@example.com", "password123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", is("Email is already in use")));
    }

    @Test
    public void testLoginSuccess() throws Exception {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("testuser@example.com");
        user.setPasswordHash(passwordEncoder.encode("password123"));
        userRepository.save(user);

        LoginRequest request = new LoginRequest("testuser", "password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.user.username", is("testuser")));
    }

    @Test
    public void testLoginBadCredentials() throws Exception {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("testuser@example.com");
        user.setPasswordHash(passwordEncoder.encode("password123"));
        userRepository.save(user);

        LoginRequest request = new LoginRequest("testuser", "wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("Invalid username or password")));
    }
}
