package com.hotelpos.demo.features.auth;

import com.hotelpos.demo.core.tenant.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/auth") // Standardized root path
//@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShiftLogRepository shiftLogRepository;

    // 1. Inject the global password encoder bean from your SecurityConfig
    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

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

        // 2. Fetch the active multi-tenant identifier passed by your Angular interceptor
        String activeTenantId = com.hotelpos.demo.core.tenant.TenantContext.getCurrentTenant();
        if (activeTenantId == null) {
            activeTenantId = "DEFAULT_TENANT_DEV"; // Fallback matching your seeder environment
        }

        // 3. Fetch ONLY the employees belonging to this specific hotel workspace context
        List<User> activeTenantStaff = userRepository.findByTenantId(activeTenantId);
        User authenticatedUser = null;

        // 4. Trace the staff list using your password matcher bean to safely isolate the account
        for (User user : activeTenantStaff) {
            if (passwordEncoder.matches(pin, user.getPinCode())) {
                authenticatedUser = user;
                break; // Target identified, terminate lookup evaluation scanner loop!
            }
        }

        Map<String, Object> jsonResponse = new HashMap<>();

        // 5. Evaluate authentication outcome profile structures
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
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).body(jsonResponse);
        }
    }
}
