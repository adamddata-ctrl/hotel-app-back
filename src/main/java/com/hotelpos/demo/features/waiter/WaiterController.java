package com.hotelpos.demo.features.waiter;

import com.hotelpos.demo.core.tenant.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/waiters") // Fixed: Clean, absolute base path
//@CrossOrigin(origins = "http://localhost:4200") // Prevents browser CORS blocks
public class WaiterController {

    @Autowired
    private WaiterRepository waiterRepository;
    /**
     * Resolves active waiters working shifts. Automatically scopes query lookups
     * using the current thread's ThreadLocal multi-tenant identity constraints.
     */
    @GetMapping("/active") // Fixed: Clean sub-path mapping
    public ResponseEntity<List<Waiter>> getActiveWaitersByTenant() {
        // 1. Fixed: Extracted the active tenant string from the context thread
        String activeTenantId = TenantContext.getCurrentTenant();

        // 2. Query only the active waiters belonging to this specific restaurant space
        List<Waiter> waiters = waiterRepository.findByTenantIdAndActiveTrue(activeTenantId);

        return ResponseEntity.ok(waiters);
    }
    /**
     * Quick action shortcut to instantly onboard new staff directly from the frontend UI modal.
     */
    @PostMapping("/create") // Fixed: Removed invalid mapping wildcards
    public ResponseEntity<Waiter> createWaiter(@RequestBody Waiter waiterPayload) {
        String activeTenantId = TenantContext.getCurrentTenant();

        Waiter waiter = new Waiter();
        waiter.setTenantId(activeTenantId); // Secure multi-tenant anchoring assignment
        waiter.setWaiterName(waiterPayload.getWaiterName());
        waiter.setActive(true);

        Waiter savedWaiter = waiterRepository.save(waiter);
        return ResponseEntity.ok(savedWaiter);
    }
}