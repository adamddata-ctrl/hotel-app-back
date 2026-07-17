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


Optional<User> findByTenantIdAndUsername(String tenantId, String username);


    Optional<User> findByTenantIdAndPinCode(String tenantId, String pinCode);
}