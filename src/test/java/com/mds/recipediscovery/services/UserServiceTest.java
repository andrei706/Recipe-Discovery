package com.mds.recipediscovery.services;

import com.mds.recipediscovery.dto.ChangePasswordRequestDTO;
import com.mds.recipediscovery.dto.LoginRequestDTO;
import com.mds.recipediscovery.dto.LoginResponseDTO;
import com.mds.recipediscovery.dto.SignupRequestDTO;
import com.mds.recipediscovery.dto.SignupResponseDTO;
import com.mds.recipediscovery.models.User;
import com.mds.recipediscovery.repository.UserRepository;
import com.mds.recipediscovery.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;

    private UserService userService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", "test-secret-key-with-at-least-32-characters");
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", 3600000L);
        userService = new UserService(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void loginWithUsernameAndBcryptPassword_ReturnsToken() {
        User user = new User();
        user.setUserId(1);
        user.setUsername("Andrei");
        user.setEmail("andrei@email.com");
        user.setPassword(passwordEncoder.encode("passAndrei1"));

        LoginRequestDTO request = new LoginRequestDTO();
        request.setLoginType("USERNAME");
        request.setIdentifier("Andrei");
        request.setPassword("passAndrei1");

        when(userRepository.findByUsername("Andrei")).thenReturn(Optional.of(user));

        LoginResponseDTO response = userService.login(request);

        assertTrue(response.getToken() != null && !response.getToken().isBlank());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(1, response.getUserId());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginWithPlainTextStoredPassword_ThrowsSecurityException() {
        User user = new User();
        user.setUserId(1);
        user.setUsername("Andrei");
        user.setEmail("andrei@email.com");
        user.setPassword("passAndrei1");

        LoginRequestDTO request = new LoginRequestDTO();
        request.setLoginType("USERNAME");
        request.setIdentifier("Andrei");
        request.setPassword("passAndrei1");

        when(userRepository.findByUsername("Andrei")).thenReturn(Optional.of(user));

        assertThrows(SecurityException.class, () -> userService.login(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginWithEmailAndWrongPassword_ThrowsSecurityException() {
        User user = new User();
        user.setUserId(2);
        user.setUsername("Elena");
        user.setEmail("elena@email.com");
        user.setPassword(passwordEncoder.encode("passElena2"));

        LoginRequestDTO request = new LoginRequestDTO();
        request.setLoginType("EMAIL");
        request.setIdentifier("elena@email.com");
        request.setPassword("wrongPassword");

        when(userRepository.findByEmail("elena@email.com")).thenReturn(Optional.of(user));

        assertThrows(SecurityException.class, () -> userService.login(request));
    }

    @Test
    void signupWithValidData_CreatesUserAndReturnsToken() {
        SignupRequestDTO request = new SignupRequestDTO();
        request.setUsername("Maria");
        request.setEmail("maria@email.com");
        request.setPassword("mariaPass123");

        when(userRepository.findByUsername("Maria")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("maria@email.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setUserId(10);
            return user;
        });

        SignupResponseDTO response = userService.signup(request);

        assertTrue(response.getToken() != null && !response.getToken().isBlank());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(10, response.getUserId());
        assertEquals("Maria", response.getUsername());
        assertEquals("maria@email.com", response.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void signupWithExistingUsername_ThrowsIllegalArgumentException() {
        SignupRequestDTO request = new SignupRequestDTO();
        request.setUsername("Andrei");
        request.setEmail("new@email.com");
        request.setPassword("andreiPass123");

        User existingUser = new User();
        when(userRepository.findByUsername("Andrei")).thenReturn(Optional.of(existingUser));

        assertThrows(IllegalArgumentException.class, () -> userService.signup(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changePassword_WithValidCurrentPassword_UpdatesHash() {
        User user = new User();
        user.setUserId(3);
        user.setPassword(passwordEncoder.encode("oldPassword123"));

        ChangePasswordRequestDTO request = new ChangePasswordRequestDTO();
        request.setCurrentPassword("oldPassword123");
        request.setNewPassword("newPassword123");

        when(userRepository.findById(3)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.changePassword(3, request);

        assertTrue(passwordEncoder.matches("newPassword123", user.getPassword()));
        verify(userRepository).save(user);
    }

    @Test
    void changePassword_WithInvalidCurrentPassword_ThrowsSecurityException() {
        User user = new User();
        user.setUserId(3);
        user.setPassword(passwordEncoder.encode("oldPassword123"));

        ChangePasswordRequestDTO request = new ChangePasswordRequestDTO();
        request.setCurrentPassword("wrongOldPassword");
        request.setNewPassword("newPassword123");

        when(userRepository.findById(3)).thenReturn(Optional.of(user));

        assertThrows(SecurityException.class, () -> userService.changePassword(3, request));
        verify(userRepository, never()).save(any(User.class));
    }
}

