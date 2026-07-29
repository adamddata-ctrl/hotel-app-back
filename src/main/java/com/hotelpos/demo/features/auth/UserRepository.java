package com.hotelpos.demo.features.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository // <-- Add this annotation right here

public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByUsername(String username);
    // 🎯 ADD THIS LINE: Tells Spring Data JPA to generate the multi-tenant finder query method
    Optional<User> findByTenantIdAndPinCode(String tenantId, String pinCode);

    // Useful for the localized scanning loop method we discussed earlier
    List<User> findByTenantId(String tenantId);
}