package com.mds.recipediscovery.controller;

import com.mds.recipediscovery.dto.ChangePasswordRequestDTO;
import com.mds.recipediscovery.dto.LoginRequestDTO;
import com.mds.recipediscovery.dto.LoginResponseDTO;
import com.mds.recipediscovery.dto.LogoutResponseDTO;
import com.mds.recipediscovery.dto.SignupRequestDTO;
import com.mds.recipediscovery.dto.SignupResponseDTO;
import com.mds.recipediscovery.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO request) {
        try {
            return ResponseEntity.ok(userService.login(request));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (SecurityException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<SignupResponseDTO> signup(@RequestBody SignupRequestDTO request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(userService.signup(request));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<LogoutResponseDTO> logout() {
        return ResponseEntity.ok(new LogoutResponseDTO("Logout reusit. Sterge token-ul din client (localStorage/sessionStorage)."));
    }

    @PostMapping("/change-password")
    public ResponseEntity<LogoutResponseDTO> changePassword(Authentication authentication,
                                                            @RequestBody ChangePasswordRequestDTO request) {
        try {
            if (authentication == null || authentication.getName() == null) {
                throw new SecurityException("Utilizator neautentificat");
            }
            Integer userId = Integer.valueOf(authentication.getName());
            userService.changePassword(userId, request);
            return ResponseEntity.ok(new LogoutResponseDTO("Parola a fost schimbata cu succes."));
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token invalid");
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (SecurityException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }
}

