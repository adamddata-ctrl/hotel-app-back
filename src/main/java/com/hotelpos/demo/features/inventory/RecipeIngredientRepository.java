package com.hotelpos.demo.features.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredient, Long> {

    // Finds all ingredients mapped to a specific menu item ID (useful for Option B calculations)
    List<RecipeIngredient> findByMenuItemId(Integer menuItemId);
}