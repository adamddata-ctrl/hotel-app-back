package com.hotelpos.demo.features.auth;

import com.hotelpos.demo.core.tenant.TenantContext;
import com.hotelpos.demo.features.restaurant.Restaurant;
import com.hotelpos.demo.features.restaurant.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TenantRegistrationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public String registerNewRestaurant(TenantRegistrationDto dto) {
        // Generate a unique tenant ID
        String uniqueTenantId = "TNT_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // 🔥 CRITICAL FIX: Bind the new tenant ID to the thread context BEFORE saving any entities
        TenantContext.setCurrentTenant(uniqueTenantId);

        try {
            // =========================================================
            // 1. CREATE AND SAVE THE RESTAURANT (TENANT) RECORD
            // =========================================================
            Restaurant restaurant = new Restaurant();
            restaurant.setId(uniqueTenantId);
            restaurant.setName(dto.getFullName());
            restaurant.setOwnerEmail(dto.getUsername() + "@restaurant.local");
            restaurant.setActive(true);
            restaurantRepository.save(restaurant);

            // =========================================================
            // 2. CREATE THE OWNER / MANAGER USER
            // =========================================================
            User manager = new User();
            manager.setId("USR-MGMT-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
            manager.setTenantId(uniqueTenantId);
            manager.setUsername(dto.getUsername());
            manager.setRole(User.Role.OWNER);
            manager.setPassword(passwordEncoder.encode(dto.getPassword()));
            userRepository.save(manager);

            // =========================================================
            // 3. CREATE THE DEFAULT CASHIER USER
            // =========================================================
            User cashier = new User();
            cashier.setId("USR-CASH-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
            cashier.setTenantId(uniqueTenantId);
            cashier.setUsername(dto.getFullName() + "_Cashier");
            cashier.setRole(User.Role.CASHIER);

            String cashierPin = dto.getPinCode();
            if (cashierPin == null || cashierPin.isEmpty()) {
                cashierPin = "1234"; // Fallback default
            }
            cashier.setPinCode(passwordEncoder.encode(cashierPin));
            userRepository.save(cashier);

            return uniqueTenantId;
        } finally {
            // 🔥 CRITICAL FIX: Clear the context after the transaction is complete
            TenantContext.clear();
        }
    }
}