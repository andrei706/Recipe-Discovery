package com.mds.recipediscovery.controller;

import com.mds.recipediscovery.dto.AiRequestDTO;
import com.mds.recipediscovery.services.AiService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> getChatResponse(
            @RequestBody AiRequestDTO request,
            Authentication authentication) {
        try {
            Integer userId = getCurrentUserId(authentication);
            Map<String, Object> response = aiService.getChatResponse(userId, request.getPrompt());
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    private Integer getCurrentUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(UNAUTHORIZED, "User not authenticated");
        }

        try {
            return Integer.valueOf(authentication.getName());
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid token");
        }
    }
}
