
package com.hotelpos.demo.features.waiter;

import com.hotelpos.demo.core.tenant.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/waiters")
public class WaiterController {

    @Autowired
    private WaiterRepository waiterRepository;

    @GetMapping("/active")
    public ResponseEntity<List<Waiter>> getActiveWaitersByTenant() {
        String activeTenantId = TenantContext.getCurrentTenant();

        // Safety validation fallback if interceptor context is uninitialized
        if (activeTenantId == null || activeTenantId.trim().isEmpty()) {
            activeTenantId = "TNT_989D7F02";
        }

        List<Waiter> waiters = waiterRepository.findByTenantIdAndActiveTrue(activeTenantId);
        return ResponseEntity.ok(waiters);
    }

    @PostMapping("/create")
    public ResponseEntity<Waiter> createWaiter(@RequestBody Waiter waiterPayload) {
        String activeTenantId = TenantContext.getCurrentTenant();

        // Safety validation fallback if interceptor context is uninitialized
        if (activeTenantId == null || activeTenantId.trim().isEmpty()) {
            activeTenantId = "TNT_989D7F02";
        }

        Waiter waiter = new Waiter();
        waiter.setTenantId(activeTenantId);
        waiter.setWaiterName(waiterPayload.getWaiterName());
        waiter.setActive(true);

        Waiter savedWaiter = waiterRepository.save(waiter);
        return ResponseEntity.ok(savedWaiter);
    }
}