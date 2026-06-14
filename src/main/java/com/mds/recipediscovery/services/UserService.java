package com.mds.recipediscovery.services;

import com.mds.recipediscovery.dto.ChangePasswordRequestDTO;
import com.mds.recipediscovery.dto.LoginRequestDTO;
import com.mds.recipediscovery.dto.LoginResponseDTO;
import com.mds.recipediscovery.dto.SignupRequestDTO;
import com.mds.recipediscovery.dto.SignupResponseDTO;
import com.mds.recipediscovery.models.User;
import com.mds.recipediscovery.repository.UserRepository;
import com.mds.recipediscovery.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponseDTO login(LoginRequestDTO request) {
        if (request == null || isBlank(request.getIdentifier()) || isBlank(request.getPassword()) || isBlank(request.getLoginType())) {
            throw new IllegalArgumentException("loginType, identifier and password are required");
        }

        User user = findUserByLoginType(request.getLoginType(), request.getIdentifier());

        if (!matchesPassword(request.getPassword(), user)) {
            throw new SecurityException("Invalid credentials");
        }

        String token = jwtService.generateToken(user);
        return new LoginResponseDTO(
                token,
                "Bearer",
                jwtService.getJwtExpirationMs(),
                user.getUserId(),
                user.getUsername(),
                user.getEmail()
        );
    }

    public SignupResponseDTO signup(SignupRequestDTO request) {
        if (request == null || isBlank(request.getUsername()) || isBlank(request.getEmail()) || isBlank(request.getPassword())) {
            throw new IllegalArgumentException("username, email and password are required");
        }

        String username = request.getUsername().trim();
        String email = request.getEmail().trim();

        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("Username can only contain letters, numbers, and underscores (_), without spaces.");
        }

        if (username.length() > 50) {
            throw new IllegalArgumentException("Username must be at most 50 characters long");
        }

        if (email.length() > 100 || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email");
        }

        if (request.getPassword().length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }

        if (userRepository.findByUsernameIgnoreCase(username).isPresent()) {
            throw new IllegalArgumentException("Username already in use");
        }

        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);
        String token = jwtService.generateToken(savedUser);

        return new SignupResponseDTO(
                token,
                "Bearer",
                jwtService.getJwtExpirationMs(),
                savedUser.getUserId(),
                savedUser.getUsername(),
                savedUser.getEmail()
        );
    }

    public User getUserById(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public void changePassword(Integer userId, ChangePasswordRequestDTO request) {
        if (request == null || isBlank(request.getCurrentPassword()) || isBlank(request.getNewPassword())) {
            throw new IllegalArgumentException("currentPassword and newPassword are required");
        }

        if (request.getNewPassword().length() < 8) {
            throw new IllegalArgumentException("New password must be at least 8 characters long");
        }

        User user = getUserById(userId);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new SecurityException("Current password is invalid");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private User findUserByLoginType(String loginType, String identifier) {
        String normalizedType = loginType.trim().toUpperCase();

        return switch (normalizedType) {
            case "USERNAME" -> userRepository.findByUsernameIgnoreCase(identifier)
                    .orElseThrow(() -> new SecurityException("Invalid credentials"));
            case "EMAIL" -> userRepository.findByEmailIgnoreCase(identifier)
                    .orElseThrow(() -> new SecurityException("Invalid credentials"));
            default -> throw new IllegalArgumentException("loginType must be USERNAME or EMAIL");
        };
    }

    private boolean matchesPassword(String rawPassword, User user) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

