package com.hotelpos.demo.features.restaurant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Data Access Object layer for the restaurants table. Inherits
 * comprehensive CRUD capabilities directly from JpaRepository.
 */
@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, String> {
    /**
     * Finds a registered enterprise business tenant by its primary email identifier.
     * Used for tenancy authentication validations.
     */
    Optional<Restaurant> findByOwnerEmail(String ownerEmail);
}