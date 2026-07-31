package com.hotelpos.demo.features.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByUsername(String username);
    Optional<User> findByTenantIdAndPinCode(String tenantId, String pinCode); // ✅
    List<User> findByTenantId(String tenantId); // ✅ Inside interface body
}