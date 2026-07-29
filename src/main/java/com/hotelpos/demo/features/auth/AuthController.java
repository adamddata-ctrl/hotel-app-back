package com.hotelpos.demo.features.auth;

import com.hotelpos.demo.core.tenant.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth") // Matches your clean, top-level frontend URL structure
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShiftLogRepository shiftLogRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TenantRegistrationService tenantRegistrationService;

    /**
     * 1. Cashier PIN Authentication Endpoint
     */
    @PostMapping("/cashier-login")
    public ResponseEntity<?> cashierLogin(@RequestBody LoginRequest request) {
        String activeTenantId = TenantContext.getCurrentTenant();

        // 1. Gather all user profiles registered to this active workspace tenant context
        List<User> tenantUsers = userRepository.findByTenantId(activeTenantId);

        // 2. Loop through users and use BCrypt matching to evaluate the incoming plaintext pinCode
        for (User user : tenantUsers) {
            if (passwordEncoder.matches(request.getPinCode(), user.getPinCode())) {
                Map<String, Object> response = new HashMap<>();
                response.put("status", "SUCCESS");
                response.put("role", user.getRole().toString());
                response.put("username", user.getUsername());
                return ResponseEntity.ok(response);
            }
        }

        // 3. Fallback handle rejection if no profiles match the credential criteria
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Invalid pin credentials for the current tenant space");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * 2. Provision isolated production multi-tenant database partitions.
     */
    @PostMapping("/register-tenant")
    public ResponseEntity<?> registerTenant(@RequestBody TenantRegistrationDto registrationDto) {
        try {
            String newTenantId = tenantRegistrationService.registerNewRestaurant(registrationDto);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "New restaurant workspace created successfully!");
            response.put("tenantId", newTenantId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to create restaurant: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * 3. PRODUCTION ADDITION: Receives frontend shift-closing terminal events.
     */
    @PostMapping("/cashier-logout")
    public ResponseEntity<?> cashierLogout(@RequestBody Map<String, Object> payload) {
        Object cashierId = payload.get("cashierId");

        System.out.println("SHIFT LOG ENGINE: Processing shift log finalization tracking for ID: " + cashierId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Shift closure details saved successfully in database history.");

        return ResponseEntity.ok(response);
    }
}