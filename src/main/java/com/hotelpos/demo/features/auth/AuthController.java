package com.hotelpos.demo.features.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200") // Prevents browser CORS blocks
public class AuthController {

    @Autowired
    private UserRepository userRepository;
    /**
     * Listens for the 4-digit PIN code pushed from Angular's cashier pin-pad login interface.
     */
    @PostMapping("/cashier-login")
    public ResponseEntity<?> cashierLogin(@RequestBody Map<String, String> request) {
        String pin = request.get("pin");

        // Search through the users database table for a matching Cashier security PIN
        Optional<User> userOpt = userRepository.findAll().stream()
                .filter(u -> pin.equals(u.getPinCode()) && u.getRole() == User.Role.CASHIER)
                .findFirst();

        Map<String, Object> response = new HashMap<>();

        if (userOpt.isPresent()) {
            User cashier = userOpt.get();
            response.put("success", true);
            response.put("tenantId", cashier.getTenantId());
            response.put("cashierId", cashier.getId());
            response.put("cashierName", cashier.getDisplayName());
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Invalid Cashier Security PIN");
            return ResponseEntity.status(401).body(response);
        }
    }
}