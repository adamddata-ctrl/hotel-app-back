package com.hotelpos.demo.features.waiter;

import com.hotelpos.demo.core.tenant.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/waiters") // Matches your clean, unified frontend URL layout
public class WaiterController {

    @Autowired
    private WaiterRepository waiterRepository;

    /**
     * Fetches active waiters belonging strictly to the currently active tenant session context.
     */
    @GetMapping("/active")
    public ResponseEntity<?> getActiveWaitersByTenant() {
        String activeTenantId = TenantContext.getCurrentTenant();

        // SECURE FAIL CLOSED: Rejects the request if no valid tenant context header is provided
        if (activeTenantId == null || activeTenantId.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "Security Violation: Tenant identification missing."));
        }

        // Fetches waiters isolated by the active tenant ID from the database partition
        List<Waiter> waiters = waiterRepository.findByTenantIdAndActiveTrue(activeTenantId);
        return ResponseEntity.ok(waiters);
    }

    /**
     * Provisions a brand new waiter entry under the active tenant's workspace partition.
     */
    @PostMapping("/create")
    public ResponseEntity<?> createWaiter(@RequestBody Waiter waiterPayload) {
        String activeTenantId = TenantContext.getCurrentTenant();

        if (activeTenantId == null || activeTenantId.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "Security Violation: Unassigned tenant scope context."));
        }

        Waiter waiter = new Waiter();
        waiter.setWaiterName(waiterPayload.getWaiterName());
        waiter.setTenantId(activeTenantId); // Forces the record to save under the isolated tenant block
        waiter.setActive(true);

        Waiter savedWaiter = waiterRepository.save(waiter);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedWaiter);
    }
}