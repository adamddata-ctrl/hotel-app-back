package com.hotelpos.demo.features.checkout;

import com.hotelpos.demo.core.tenant.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/checkout")
@CrossOrigin(origins = "http://localhost:4200")
public class CheckoutController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private com.hotelpos.demo.features.menu.MenuItemRepository menuItemRepository;

    @Autowired
    private com.hotelpos.demo.features.inventory.InventoryService inventoryService;

    /**
     * Receives order ticket streams from your Angular touchscreen terminal grid workspace.
     */
    @PostMapping("/order")
    @Transactional // Ensures atomic multi-table execution data rollbacks on execution faults
    public ResponseEntity<?> checkoutOrder(@RequestBody OrderCreateRequest request) {

        if (request.getItems() == null || request.getItems().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cannot checkout an empty order"));
        }

        try {
            // 1. Instantiates your Master Parent record mapping details
            Order order = new Order();
            order.setCashierId(request.getCashierId());
            order.setWaiterId(request.getWaiterId());
            order.setTotalAmount(request.getTotalAmount());
            // 2. Maps the incoming payload array items to our relational entities
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
            // CascadeType.ALL handles writing to both 'orders' and 'order_items' instantly
            Order savedOrder = orderRepository.save(order);
            // ========================================================
            // ========================================================
            // ADDED: Deduct stock metrics automatically for all items
            // ========================================================
            if (savedOrder.getItems() != null) {
                for (OrderItem item : savedOrder.getItems()) {
                    menuItemRepository.findById(item.getItemId()).ifPresent(menuItem -> {
                        inventoryService.deductStockForOrder(menuItem, item.getQuantity());
                    });
                }
            }
            // ========================================================

            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("success", true);
            successResponse.put("orderId", savedOrder.getId());
            successResponse.put("message", "Transaction archived. Kitchen routing ticket released.");

            return ResponseEntity.ok(successResponse);

        } catch (Exception ex) {
            return ResponseEntity.status(500).body(Map.of(
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
            String activeTenantId = com.hotelpos.demo.core.tenant.TenantContext.getCurrentTenant();

            // 2. Fetch order entities filtered by the active tenant boundary
            java.util.List<Order> openOrders = orderRepository.findByTenantId(activeTenantId);

            // 3. Map database relations down onto lightweight scannable JSON KDS array matrices
            java.util.List<java.util.Map<String, Object>> kdsPayload = new java.util.ArrayList<>();

            for (Order order : openOrders) {
                java.util.Map<String, Object> ticket = new java.util.HashMap<>();
                ticket.put("id", order.getId());
                ticket.put("waiterName", "Server #" + order.getWaiterId()); // Fixed: Maps to your correct quantity properties
                ticket.put("orderTime", order.getCreatedAt() != null ? order.getCreatedAt().toLocalTime() : null);

                // Nest line item quantities and descriptions cleanly
                java.util.List<java.util.Map<String, Object>> itemsList = new java.util.ArrayList<>();
                for (OrderItem item : order.getItems()) {
                    java.util.Map<String, Object> itemData = new java.util.HashMap<>();

                    // Look up the clear-text item name description string from your menu repository utility
                    String actualItemName = menuItemRepository.findById(item.getItemId())
                            .map(com.hotelpos.demo.features.menu.MenuItem::getItemName)
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
            return ResponseEntity.status(500).body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/orders/fulfill/{id}")
    public ResponseEntity<?> fulfillKitchenOrderTicket(@PathVariable("id") Integer orderId) {
        String activeTenantId = com.hotelpos.demo.core.tenant.TenantContext.getCurrentTenant();

        return orderRepository.findById(orderId)
                .map(order -> {
                    // Cryptographic Boundary Check: Prevent cross-tenant database profile pollution logic
                    if (!order.getTenantId().equals(activeTenantId)) {
                        return ResponseEntity.status(403).body(java.util.Map.of("error", "Access Denied"));
                    }

                    // Delete the ticket directly from the active kitchen table row registry
                    orderRepository.delete(order);
                    java.util.Map<String, Object> response = new java.util.HashMap<>();
                    response.put("success", true);
                    response.put("message", "Ticket successfully dispatched from production lines.");
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> ResponseEntity.status(404).body(java.util.Map.of("error", "Target ticket context missing")));
    }
}