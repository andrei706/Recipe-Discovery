package com.mds.recipediscovery.controller;

import com.mds.recipediscovery.dto.IngredientDTO;
import com.mds.recipediscovery.repository.IngredientRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ingredients")
@CrossOrigin(origins = "*")
public class IngredientController {

    private final IngredientRepository ingredientRepository;

    public IngredientController(IngredientRepository ingredientRepository) {
        this.ingredientRepository = ingredientRepository;
    }

    @GetMapping
    public ResponseEntity<List<IngredientDTO>> getIngredients() {
        return ResponseEntity.ok(ingredientRepository.findAll(Sort.by("name"))
                .stream()
                .map(IngredientDTO::new)
                .toList());
    }
}

