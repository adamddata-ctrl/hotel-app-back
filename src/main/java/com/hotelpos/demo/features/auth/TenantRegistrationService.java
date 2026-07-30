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
        String uniqueTenantId = "TNT_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // Owner/Manager
        User manager = new User();
        manager.setId("USR-MGMT-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        manager.setTenantId(uniqueTenantId);
        manager.setUsername(dto.getUsername());              // ✅ Changed from getOwnerUsername()
        manager.setRole(User.Role.OWNER);
        manager.setPassword(passwordEncoder.encode(dto.getPassword())); // ✅ Changed from getOwnerPassword()
        userRepository.save(manager);

        // Cashier
        User cashier = new User();
        cashier.setId("USR-CASH-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        cashier.setTenantId(uniqueTenantId);
        cashier.setUsername(dto.getFullName() + "_Cashier"); // ✅ Changed from getRestaurantName()
        cashier.setRole(User.Role.CASHIER);

        String cashierPin = dto.getPinCode();                // ✅ Changed from getDefaultCashierPin()
        if (cashierPin == null || cashierPin.isEmpty()) {
            cashierPin = "1234";
        }
        cashier.setPinCode(passwordEncoder.encode(cashierPin));
        userRepository.save(cashier);

        return uniqueTenantId;
    }
}