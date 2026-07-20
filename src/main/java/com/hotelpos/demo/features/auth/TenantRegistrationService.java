package com.hotelpos.demo.features.auth;

import com.hotelpos.demo.features.auth.User;
import com.hotelpos.demo.features.auth.UserRepository;
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
        manager.setUsername(dto.getUsername());
        manager.setRole(User.Role.OWNER); // Set to your matching enum or string entity property mapping
        manager.setPinCode(passwordEncoder.encode(dto.getPinCode()));

        // 3. Persist the database record to your active Railway MySQL instance
        userRepository.save(manager);

        return uniqueTenantId;
    }
}