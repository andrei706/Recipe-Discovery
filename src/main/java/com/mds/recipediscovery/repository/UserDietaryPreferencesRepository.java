package com.mds.recipediscovery.repository;

import com.mds.recipediscovery.models.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserDietaryPreferencesRepository extends JpaRepository<UserDietaryPreferences, UserDietaryPreferencesId> {
    List<UserDietaryPreferences> findByUser(User user);
}
