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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/checkout") // Standardized clean top-level path matching our project pattern
public class CheckoutController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private InventoryService inventoryService;

    /**
     * Receives order ticket streams from your Angular touchscreen terminal grid workspace.
     */
    @PostMapping("/order")
    @Transactional // Ensures atomic multi-table execution data rollbacks on execution faults
    public ResponseEntity<?> checkoutOrder(@RequestBody OrderCreateRequest request) {

        if (request.getItems() == null || request.getItems().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cannot checkout an empty order"));
        }

        // Extricate the active restaurant isolation ID securely from the thread context firewall
        String activeTenantId = TenantContext.getCurrentTenant();
        if (activeTenantId == null || activeTenantId.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Security Violation: Target tenant context missing."));
        }

        try {
            // 1. Instantiates your Master Parent record mapping details
            Order order = new Order();
            order.setTenantId(activeTenantId); // CRITICAL MULTI-TENANT FIX: Encapsulates records under isolated tenant block
            order.setCashierId(request.getCashierId());
            order.setWaiterId(request.getWaiterId());
            order.setTotalAmount(request.getTotalAmount());

            // 2. Maps the incoming payload array items out to your relational entities
            List<OrderItem> orderItems = new ArrayList<>();
            for (OrderItemRequest itemReq : request.getItems()) {
                OrderItem detailItem = new OrderItem();
                detailItem.setOrder(order); // Crucial: Explicitly binds parent entity to child record
                detailItem.setItemId(itemReq.getItemId());
                detailItem.setQuantity(itemReq.getQuantity());
                detailItem.setUnitPrice(itemReq.getUnitPrice());
                orderItems.add(detailItem);
            }

            // Bind list array data maps back down onto primary schema model instance
            order.setItems(orderItems);

            // 3. Persist transaction context logs down onto your physical database layer
            Order savedOrder = orderRepository.save(order);

            // ADDED: Deduct stock metrics automatically for all items
            if (savedOrder.getItems() != null) {
                for (OrderItem item : savedOrder.getItems()) {
                    menuItemRepository.findById(item.getItemId()).ifPresent(menuItem -> {
                        inventoryService.deductStockForOrder(menuItem, item.getQuantity());
                    });
                }
            }

            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("success", true);
            successResponse.put("orderId", savedOrder.getId());
            successResponse.put("message", "Transaction archived. Kitchen routing ticket released.");

            return ResponseEntity.ok(successResponse);

        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "Transaction isolation breakdown fault",
                    "details", ex.getMessage()
            ));
        }
    }

    /**
     * Streams all active unfulfilled kitchen tickets for the requesting restaurant.
     */
    @GetMapping("/orders/open")
    public ResponseEntity<?> getOpenKitchenOrders() {
        try {
            // 1. Extricate the active restaurant isolation ID securely from the thread context firewall
            String activeTenantId = TenantContext.getCurrentTenant();
            if (activeTenantId == null || activeTenantId.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Access Denied: Missing multi-tenant identification context."));
            }

            // 2. Fetch order entities filtered by the active tenant boundary
            List<Order> openOrders = orderRepository.findByTenantId(activeTenantId);

            // 3. Map database relations down onto lightweight scannable JSON KDS array matrices
            List<Map<String, Object>> kdsPayload = new ArrayList<>();

            for (Order order : openOrders) {
                Map<String, Object> ticket = new HashMap<>();
                ticket.put("id", order.getId());
                ticket.put("waiterName", "Server #" + order.getWaiterId()); // Fixed: Maps to your correct properties parameters
                ticket.put("orderTime", order.getCreatedAt() != null ? order.getCreatedAt().toLocalTime() : null);

                // Nest line item quantities and descriptions cleanly
                List<Map<String, Object>> itemsList = new ArrayList<>();
                for (OrderItem item : order.getItems()) {
                    Map<String, Object> itemData = new HashMap<>();

                    // Look up the clear-text item name description string from your menu repository utility
                    String actualItemName = menuItemRepository.findById(item.getItemId())
                            .map(MenuItem::getItemName)
                            .orElse("Unknown Product");

                    itemData.put("itemName", actualItemName);
                    itemData.put("quantity", item.getQuantity()); // Fixed: Maps to your correct quantities parameters
                    itemsList.add(itemData);
                }

                ticket.put("items", itemsList);
                kdsPayload.add(ticket);
            }

            return ResponseEntity.ok(kdsPayload);

        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", ex.getMessage()
            ));
        }
    }

    /**
     * Cryptographic Boundary Check: Prevent cross-tenant database profile pollution logic
     */
    @PostMapping("/orders/fulfill/{id}") // Path variable match token
    public ResponseEntity<?> fulfillKitchenOrderTicket(@PathVariable("id") Integer id) { // FIXED: Binds template variables parameter naming precisely
        String activeTenantId = TenantContext.getCurrentTenant();

        if (activeTenantId == null || activeTenantId.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Access Denied: Tenant validation failed."));
        }

        return orderRepository.findById(id)
                .map(order -> {
                    // Cryptographic Boundary Check: Prevent cross-tenant database profile pollution logic
                    if (!order.getTenantId().equals(activeTenantId)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body((Object) Map.of("error", "Access Denied"));
                    }

                    // Delete the ticket directly from the active kitchen table row registry
                    orderRepository.delete(order);

                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("message", "Ticket successfully dispatched from production lines.");

                    return ResponseEntity.ok((Object) response);
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Target ticket context not found")));
    }
}