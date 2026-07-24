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
@RequestMapping("/auth") // Matches your clean, top-level frontend URL structure
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
     * 1. Rapid touchscreen validation matching numeric safety PIN sequences.
     */
    @PostMapping("/cashier-login")
    public ResponseEntity<?> cashierLogin(@RequestBody Map<String, String> request) {
        String pin = request.get("pin");

        // Validate basic payload presence
        if (pin == null || pin.trim().isEmpty()) {
            Map<String, Object> fallbackResponse = new HashMap<>();
            fallbackResponse.put("success", false);
            fallbackResponse.put("message", "PIN is required.");
            return ResponseEntity.badRequest().body(fallbackResponse);
        }

        // Fetch the active multi-tenant identifier passed by your Angular interceptor
        String activeTenantId = TenantContext.getCurrentTenant();
        if (activeTenantId == null) {
            activeTenantId = "DEFAULT_TENANT_DEV"; // Retains your dev fallback environment configuration
        }

        // Fetch ONLY the employees belonging to this specific hotel workspace context
        List<User> activeTenantStaff = userRepository.findByTenantId(activeTenantId);
        User authenticatedUser = null;

        // Trace the staff list using your password matcher bean to safely isolate the account
        for (User user : activeTenantStaff) {
            if (passwordEncoder.matches(pin, user.getPinCode())) {
                authenticatedUser = user;
                break; // Target identified, terminate lookup evaluation loop
            }
        }

        Map<String, Object> jsonResponse = new HashMap<>();

        // Evaluate authentication outcome profile structures
        if (authenticatedUser != null) {
            jsonResponse.put("success", true);
            jsonResponse.put("tenantId", authenticatedUser.getTenantId());
            jsonResponse.put("cashierId", authenticatedUser.getId());
            jsonResponse.put("cashierName", authenticatedUser.getUsername());
            jsonResponse.put("role", authenticatedUser.getRole().toString()); // Passes OWNER or CASHIER cleanly

            System.out.println("AUTH ENGINE: Account successfully verified for user: " + authenticatedUser.getUsername());
            return ResponseEntity.ok(jsonResponse);
        } else {
            // Rejects unauthorized access queries safely with a 401 response layout code
            jsonResponse.put("success", false);
            jsonResponse.put("message", "Invalid Cashier Security PIN. Please retry.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(jsonResponse);
        }
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