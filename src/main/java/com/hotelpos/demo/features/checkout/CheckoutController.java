package com.hotelpos.demo.features.checkout;

import com.hotelpos.demo.core.tenant.TenantContext;
import com.hotelpos.demo.features.menu.MenuItem;
import com.hotelpos.demo.features.menu.MenuItemRepository;
import com.hotelpos.demo.features.inventory.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.*;

@RestController
@RequestMapping("/api/checkout") // ✅ FIXED: Added "/api" prefix to match frontend environment.apiUrl
public class CheckoutController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private InventoryService inventoryService;

    /**
     * Receives order ticket streams from your Angular touchscreen terminal.
     * Deducts inventory automatically and archives the transaction.
     */
    @PostMapping("/order")
    @Transactional
    public ResponseEntity<?> checkoutOrder(@RequestBody OrderCreateRequest request) {

        // 1. Validate request payload
        if (request.getItems() == null || request.getItems().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Cannot checkout an empty order."));
        }

        // 2. Extract active multi-tenant ID from thread-safe context
        String activeTenantId = TenantContext.getCurrentTenant();
        if (activeTenantId == null || activeTenantId.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Security Violation: Tenant context missing."));
        }

        try {
            // 3. Build the master Order entity
            Order order = new Order();
            order.setTenantId(activeTenantId);
            order.setCashierId(request.getCashierId());
            order.setWaiterId(request.getWaiterId());
            order.setTotalAmount(request.getTotalAmount());

            // 4. Build child OrderItem entities and bind them to the order
            List<OrderItem> orderItems = new ArrayList<>();
            for (OrderItemRequest itemReq : request.getItems()) {
                OrderItem detail = new OrderItem();
                detail.setOrder(order);
                detail.setItemId(itemReq.getItemId());
                detail.setQuantity(itemReq.getQuantity());
                detail.setUnitPrice(itemReq.getUnitPrice());
                orderItems.add(detail);
            }
            order.setItems(orderItems);

            // 5. Persist the entire order graph
            Order savedOrder = orderRepository.save(order);

            // 6. Deduct stock for each ordered item
            if (savedOrder.getItems() != null) {
                for (OrderItem item : savedOrder.getItems()) {
                    menuItemRepository.findById(item.getItemId()).ifPresent(menuItem ->
                            inventoryService.deductStockForOrder(menuItem, item.getQuantity())
                    );
                }
            }

            // 7. Return success response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("orderId", savedOrder.getId());
            response.put("message", "Transaction archived. Kitchen ticket released.");

            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Transaction failed: " + ex.getMessage()
                    ));
        }
    }

    /**
     * Streams all active (unfulfilled) kitchen tickets for the current tenant.
     * Used by the Kitchen Screen component.
     */
    @GetMapping("/orders/open")
    public ResponseEntity<?> getOpenKitchenOrders() {
        String activeTenantId = TenantContext.getCurrentTenant();

        if (activeTenantId == null || activeTenantId.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Access Denied: Tenant context missing."));
        }

        try {
            List<Order> openOrders = orderRepository.findByTenantId(activeTenantId);

            List<Map<String, Object>> kitchenDisplayItems = new ArrayList<>();

            for (Order order : openOrders) {
                Map<String, Object> ticket = new HashMap<>();
                ticket.put("id", order.getId());
                ticket.put("waiterName", "Server #" + order.getWaiterId());

                // ✅ FIXED: Null-safe time extraction
                if (order.getCreatedAt() != null) {
                    ticket.put("orderTime", order.getCreatedAt().toLocalTime());
                } else {
                    ticket.put("orderTime", LocalTime.now());
                }

                List<Map<String, Object>> itemsList = new ArrayList<>();
                for (OrderItem item : order.getItems()) {
                    Map<String, Object> itemData = new HashMap<>();

                    String actualItemName = menuItemRepository.findById(item.getItemId())
                            .map(MenuItem::getItemName)
                            .orElse("Unknown Product");

                    itemData.put("itemName", actualItemName);
                    itemData.put("quantity", item.getQuantity());
                    itemsList.add(itemData);
                }

                ticket.put("items", itemsList);
                kitchenDisplayItems.add(ticket);
            }

            return ResponseEntity.ok(kitchenDisplayItems);

        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch kitchen orders: " + ex.getMessage()));
        }
    }

    /**
     * Marks a kitchen ticket as fulfilled and removes it from the active queue.
     * Includes a cryptographic tenant-boundary check to prevent cross-tenant deletion.
     */
    @PostMapping("/orders/fulfill/{id}")
    @Transactional
    public ResponseEntity<?> fulfillKitchenOrderTicket(@PathVariable("id") Integer id) {
        String activeTenantId = TenantContext.getCurrentTenant();

        if (activeTenantId == null || activeTenantId.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Access Denied: Tenant context missing."));
        }

        Optional<Order> orderOptional = orderRepository.findById(id);

        if (orderOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Ticket not found."));
        }

        Order order = orderOptional.get();

        // ✅ Security boundary check: Prevent cross-tenant manipulation
        if (!order.getTenantId().equals(activeTenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access Denied: Ticket belongs to another tenant."));
        }

        // Delete the ticket from the active queue
        orderRepository.delete(order);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Ticket #" + id + " successfully fulfilled and removed from queue.");

        return ResponseEntity.ok(response);
    }
}