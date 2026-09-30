package com.example.librarymgnt.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.librarymgnt.entity.LoginData;
import com.example.librarymgnt.entity.RegisterRequest;
import com.example.librarymgnt.entity.Role;
import com.example.librarymgnt.entity.User;
import com.example.librarymgnt.entity.UserResponse;
import com.example.librarymgnt.exception.AccountDisabledException;
import com.example.librarymgnt.exception.UserAlreadyExistsException;
import com.example.librarymgnt.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserService userService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest(
                "Alice Smith",
                "alicesmith",
                "alice@example.com",
                "secret123",
                "9876543210",
                "123 Main St"
        );
    }

    @Test
    void addNewUser_Success_EncodesPasswordWithBCrypt() {
        when(userRepository.existsByUsername("alicesmith")).thenReturn(false);
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("$2a$10$encodedPasswordHash");

        User savedUser = new User();
        savedUser.setUserid(1);
        savedUser.setName(registerRequest.getName());
        savedUser.setUsername(registerRequest.getUsername());
        savedUser.setEmail(registerRequest.getEmail());
        savedUser.setPassword("$2a$10$encodedPasswordHash");
        savedUser.setRole(Role.MEMBER);
        savedUser.setEnabled(true);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = userService.addNewUser(registerRequest);

        assertNotNull(response);
        assertEquals(1, response.getId());
        assertEquals("alicesmith", response.getUsername());
        assertEquals("alice@example.com", response.getEmail());
        assertEquals(Role.MEMBER, response.getRole());
        assertTrue(response.getEnabled());

        // Verify that raw password was passed to passwordEncoder and NOT null
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User captured = userCaptor.getValue();
        assertEquals("$2a$10$encodedPasswordHash", captured.getPassword());
        verify(passwordEncoder).encode("secret123");
    }

    @Test
    void addNewUser_ThrowsUserAlreadyExists_WhenUsernameExists() {
        when(userRepository.existsByUsername("alicesmith")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> userService.addNewUser(registerRequest));
    }

    @Test
    void addNewUser_ThrowsUserAlreadyExists_WhenEmailExists() {
        when(userRepository.existsByUsername("alicesmith")).thenReturn(false);
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> userService.addNewUser(registerRequest));
    }

    @Test
    void loginCheck_Success_WhenCredentialsValid() {
        User user = new User();
        user.setUserid(1);
        user.setUsername("alicesmith");
        user.setEmail("alice@example.com");
        user.setPassword("$2a$10$encodedPasswordHash");
        user.setRole(Role.MEMBER);
        user.setEnabled(true);

        when(userRepository.findByUsername("alicesmith")).thenReturn(user);
        when(passwordEncoder.matches("secret123", "$2a$10$encodedPasswordHash")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("mock.jwt.token");

        LoginData login = new LoginData("alicesmith", "secret123");
        UserResponse response = userService.loginCheck(login);

        assertNotNull(response);
        assertEquals("mock.jwt.token", response.getToken());
        assertEquals("alicesmith", response.getUsername());
    }

    @Test
    void loginCheck_ThrowsAccountDisabledException_WhenUserDisabled() {
        User user = new User();
        user.setUserid(1);
        user.setUsername("disableduser");
        user.setEmail("disabled@example.com");
        user.setPassword("$2a$10$encodedPasswordHash");
        user.setRole(Role.MEMBER);
        user.setEnabled(false);

        when(userRepository.findByUsername("disableduser")).thenReturn(user);

        LoginData login = new LoginData("disableduser", "secret123");
        assertThrows(AccountDisabledException.class, () -> userService.loginCheck(login));
    }

    @Test
    void loginCheck_ThrowsBadCredentialsException_WhenPasswordWrong() {
        User user = new User();
        user.setUserid(1);
        user.setUsername("alicesmith");
        user.setEmail("alice@example.com");
        user.setPassword("$2a$10$encodedPasswordHash");
        user.setEnabled(true);

        when(userRepository.findByUsername("alicesmith")).thenReturn(user);
        when(passwordEncoder.matches("wrongpassword", "$2a$10$encodedPasswordHash")).thenReturn(false);

        LoginData login = new LoginData("alicesmith", "wrongpassword");
        assertThrows(BadCredentialsException.class, () -> userService.loginCheck(login));
    }
}
