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
            throw new IllegalArgumentException("loginType, identifier si password sunt obligatorii");
        }

        User user = findUserByLoginType(request.getLoginType(), request.getIdentifier());

        if (!matchesPassword(request.getPassword(), user)) {
            throw new SecurityException("Credentiale invalide");
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
            throw new IllegalArgumentException("username, email si password sunt obligatorii");
        }

        String username = request.getUsername().trim();
        String email = request.getEmail().trim();

        if (username.length() > 50) {
            throw new IllegalArgumentException("Username-ul trebuie sa aiba maxim 50 de caractere");
        }

        if (email.length() > 100 || !email.contains("@")) {
            throw new IllegalArgumentException("Email invalid");
        }

        if (request.getPassword().length() < 8) {
            throw new IllegalArgumentException("Parola trebuie sa aiba minim 8 caractere");
        }

        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username deja folosit");
        }

        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email deja folosit");
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
            throw new IllegalArgumentException("currentPassword si newPassword sunt obligatorii");
        }

        if (request.getNewPassword().length() < 8) {
            throw new IllegalArgumentException("Parola noua trebuie sa aiba minim 8 caractere");
        }

        User user = getUserById(userId);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new SecurityException("Parola curenta este invalida");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private User findUserByLoginType(String loginType, String identifier) {
        String normalizedType = loginType.trim().toUpperCase();

        return switch (normalizedType) {
            case "USERNAME" -> userRepository.findByUsername(identifier)
                    .orElseThrow(() -> new SecurityException("Credentiale invalide"));
            case "EMAIL" -> userRepository.findByEmail(identifier)
                    .orElseThrow(() -> new SecurityException("Credentiale invalide"));
            default -> throw new IllegalArgumentException("loginType trebuie sa fie USERNAME sau EMAIL");
        };
    }

    private boolean matchesPassword(String rawPassword, User user) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

