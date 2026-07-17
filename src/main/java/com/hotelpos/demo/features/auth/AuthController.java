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

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {
    @Autowired
    private UserRepository userRepository;

    // 🔥 ADDED: Autowired your shift ledger tracker tool down to database schemas [3.1]
    @Autowired
    private ShiftLogRepository shiftLogRepository;

    @PostMapping("/cashier-login")
    public ResponseEntity<?> cashierLogin(@RequestBody Map<String, String> request) {
        String pin = request.get("pin");

        if (pin == null || pin.trim().isEmpty()) {
            Map<String, Object> fallbackResponse = new HashMap<>();
            fallbackResponse.put("success", false);
            fallbackResponse.put("message", "PIN is required.");
            return ResponseEntity.badRequest().body(fallbackResponse);
        }

        // 1. Safely extract the active tenant ID from the thread context firewall
        String currentTenantId = TenantContext.getCurrentTenant();

        // 2. Call your exact repository method with matching 2 arguments
        Optional<User> userOpt = userRepository.findByTenantIdAndPinCode(currentTenantId, pin);
        Map<String, Object> response = new HashMap<>();

        if (userOpt.isPresent()) {
            User cashier = userOpt.get();

            // 3. Security: Prevent owner accounts from accessing cashier POS layout
            if (cashier.getRole() != User.Role.CASHIER) {
                response.put("success", false);
                response.put("message", "Access denied. Account is not assigned a Cashier role.");
                return ResponseEntity.status(403).body(response);
            }

            // 4. 🔥 ADDED: Look for an existing running shift or spin up an automated fresh Clock-In session log [3.1]
            Optional<ShiftLog> activeShiftOpt = shiftLogRepository.findActiveShift(cashier.getId(), currentTenantId);
            if (!activeShiftOpt.isPresent()) {
                ShiftLog newShift = new ShiftLog();
                newShift.setTenantId(currentTenantId);
                newShift.setCashierId(cashier.getId());
                newShift.setCashierName(cashier.getDisplayName());
                newShift.setClockInTime(LocalDateTime.now());
                shiftLogRepository.save(newShift);
                System.out.println("🛡️ TIME CLOCK: Shift automatically started for " + cashier.getDisplayName());
            }

            // 5. Return matching fields expected by your Angular app state
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

    /**
     * 🔥 ADDED: End-point to stamp closeouts and capture billable hours [3.1]
     */
    @PostMapping("/cashier-logout")
    public ResponseEntity<?> cashierLogout(@RequestBody Map<String, String> request) {
        String cashierId = request.get("cashierId");
        String currentTenantId = TenantContext.getCurrentTenant();
        Map<String, Object> response = new HashMap<>();
        Optional<ShiftLog> activeShiftOpt = shiftLogRepository.findActiveShift(cashierId, currentTenantId);

        if (activeShiftOpt.isPresent()) {
            ShiftLog activeShift = activeShiftOpt.get();
            activeShift.setClockOutTime(LocalDateTime.now());

            // Compute precision working hours between two distinct timestamps [3.1]
            long minutesWorked = Duration.between(activeShift.getClockInTime(), activeShift.getClockOutTime()).toMinutes();
            double billableHours = (double) minutesWorked / 60.0;
            activeShift.setBillableHours(Math.round(billableHours * 100.0) / 100.0); // Rounds clean to two decimals

            shiftLogRepository.save(activeShift);
            response.put("success", true);
            response.put("message", "Shift tracked! Billable Hours: " + activeShift.getBillableHours());
            return ResponseEntity.ok(response);
        }

        response.put("success", false);
        response.put("message", "No active shift located for logging cashier.");
        return ResponseEntity.badRequest().body(response);
    }
}