package com.example.librarymgnt.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.librarymgnt.config.JwtFilter;
import com.example.librarymgnt.entity.LoginData;
import com.example.librarymgnt.entity.RegisterRequest;
import com.example.librarymgnt.entity.Role;
import com.example.librarymgnt.entity.UserResponse;
import com.example.librarymgnt.exception.AccountDisabledException;
import com.example.librarymgnt.exception.GlobalExceptionHandler;
import com.example.librarymgnt.repository.UserRepository;
import com.example.librarymgnt.service.JwtService;
import com.example.librarymgnt.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtFilter jwtFilter;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void registerUser_Success_ReturnsCreated() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "John Doe",
                "johndoe",
                "john@example.com",
                "password123",
                "1234567890",
                "City Center"
        );

        UserResponse response = new UserResponse(
                1,
                "John Doe",
                "johndoe",
                "john@example.com",
                Role.MEMBER,
                true,
                null
        );

        when(userService.addNewUser(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("johndoe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void registerUser_ValidationError_WhenFieldsMissing() throws Exception {
        RegisterRequest invalidRequest = new RegisterRequest(
                "", // Blank name
                "ab", // Too short username (< 3)
                "invalid-email", // Bad email format
                "123", // Too short password (< 6)
                "phone",
                "address"
        );

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.name").exists())
                .andExpect(jsonPath("$.validationErrors.email").exists());
    }

    @Test
    void loginCheck_Success_ReturnsOkWithToken() throws Exception {
        LoginData loginData = new LoginData("johndoe", "password123");
        UserResponse response = new UserResponse(
                1,
                "John Doe",
                "johndoe",
                "john@example.com",
                Role.MEMBER,
                true,
                "jwt.token.value"
        );

        when(userService.loginCheck(any(LoginData.class))).thenReturn(response);

        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt.token.value"));
    }

    @Test
    void loginCheck_ReturnsForbidden_WhenAccountDisabled() throws Exception {
        LoginData loginData = new LoginData("disableduser", "password123");

        when(userService.loginCheck(any(LoginData.class)))
                .thenThrow(new AccountDisabledException("User account is disabled. Please contact the administrator."));

        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginData)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("User account is disabled. Please contact the administrator."));
    }
}
