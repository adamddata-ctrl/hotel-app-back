package com.hotelpos.demo.features.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Data Access Object layer for the users table. Provides data querying methods
 * explicitly scoped by tenant boundaries to safeguard multi-tenant application access.
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

/**
 * Resolves a user by their username within a specific restaurant workspace.
 * Used principally for Tenant Owner secure sign-in operations.
 */
Optional<User> findByTenantIdAndUsername(String tenantId, String username);

    /**
     * Resolves a cashier account using their high-speed 4-digit security PIN
     * within a specific restaurant workspace boundary.
     */
    Optional<User> findByTenantIdAndPinCode(String tenantId, String pinCode);
}