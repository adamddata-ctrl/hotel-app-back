package com.hotelpos.demo.features.auth;

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
    private PasswordEncoder passwordEncoder;

    @Transactional
    public String registerNewRestaurant(TenantRegistrationDto dto) {
        // 1. Generate an isolated, random business workspace key string
        String uniqueTenantId = "TNT_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // 2. Build the primary, root MANAGER administrative staff profile
        User manager = new User();
        manager.setId("USR-MGMT-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        manager.setTenantId(uniqueTenantId);
        manager.setUsername(dto.getOwnerUsername());              // ✅ UPDATED
        manager.setRole(User.Role.OWNER);
        manager.setPassword(passwordEncoder.encode(dto.getOwnerPassword())); // ✅ UPDATED
        userRepository.save(manager);

        // 3. Build the default front-of-house CASHIER rapid terminal staff profile
        User cashier = new User();
        cashier.setId("USR-CASH-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        cashier.setTenantId(uniqueTenantId);
        cashier.setUsername(dto.getRestaurantName() + "_Cashier"); // ✅ UPDATED (uses restaurantName)
        cashier.setRole(User.Role.CASHIER);

        // ✅ Handle the cashier PIN – use the frontend value or fallback
        String cashierPin = dto.getDefaultCashierPin();
        if (cashierPin == null || cashierPin.isEmpty()) {
            cashierPin = "1234"; // fallback default PIN
        }
        cashier.setPinCode(passwordEncoder.encode(cashierPin)); // ✅ UPDATED
        userRepository.save(cashier);

        return uniqueTenantId;
    }
}