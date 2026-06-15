package com.mds.recipediscovery.controller;

import com.mds.recipediscovery.dto.InventoryItemDTO;
import com.mds.recipediscovery.models.Inventory;
import com.mds.recipediscovery.services.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "*")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/add")
    public ResponseEntity<?> addIngredient(
            Authentication authentication,
            @RequestParam Integer ingredientId,
            @RequestParam double quantity) {
        try {
            Inventory inventory = inventoryService.addIngredientToUser(getCurrentUserId(authentication), ingredientId, quantity);
            return ResponseEntity.ok(inventory);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateIngredientQuantity(
            Authentication authentication,
            @RequestParam Integer ingredientId,
            @RequestParam double newQuantity) {
        try {
            Inventory inventory = inventoryService.updateIngredientQuantity(getCurrentUserId(authentication), ingredientId, newQuantity);
            if (inventory == null) {
                return ResponseEntity.ok().body("Ingredient removed from inventory since quantity became 0");
            }
            return ResponseEntity.ok(inventory);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/remove")
    public ResponseEntity<?> removeIngredient(
            Authentication authentication,
            @RequestParam Integer ingredientId) {
        try {
            inventoryService.removeIngredientFromUser(getCurrentUserId(authentication), ingredientId);
            return ResponseEntity.ok().body("Ingredient removed successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<InventoryItemDTO>> getInventory(Authentication authentication) {
        return ResponseEntity.ok(inventoryService.getInventoryForUser(getCurrentUserId(authentication)));
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
