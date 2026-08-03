package com.hotelpos.demo.features.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredient, Long> {

    // 🔥 FIXED: Changed parameter from Integer to Long to match your MenuItem ID type
    List<RecipeIngredient> findByMenuItemId(Long menuItemId);
}