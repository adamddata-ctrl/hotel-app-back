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
        // 1. Extract the active tracking tenant identifier from the thread context execution layer
        String activeTenantId = TenantContext.getCurrentTenant();

        // 2. Fetch only the active waiters bound to this specific workspace partition
        List<Waiter> waiters = waiterRepository.findByTenantIdAndActiveTrue(activeTenantId);

        return ResponseEntity.ok(waiters);
    }

    @PostMapping("/create")
    public ResponseEntity<Waiter> createWaiter(@RequestBody Waiter waiterPayload) {
        // 1. Extract the secure tenant token from the active server transaction interceptor
        String activeTenantId = TenantContext.getCurrentTenant();

        // 2. Instantiate and map the new data profile record safely
        Waiter waiter = new Waiter();
        waiter.setTenantId(activeTenantId);
        waiter.setWaiterName(waiterPayload.getWaiterName());
        waiter.setActive(true);

        // 3. Persist the tracking state records directly to your live production database
        Waiter savedWaiter = waiterRepository.save(waiter);
        return ResponseEntity.ok(savedWaiter);
    }
}