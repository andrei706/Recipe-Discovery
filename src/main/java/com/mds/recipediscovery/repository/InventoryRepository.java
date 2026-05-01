package com.mds.recipediscovery.repository;

import com.mds.recipediscovery.models.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, InventoryId> {
    List<Inventory> findByUser(User user);
    Optional<Inventory> findByUserAndIngredient(User user, Ingredient ingredient);
}
