package com.hotelpos.demo.features.checkout;

import com.hotelpos.demo.core.tenant.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;

    /**
     * Processes a new incoming transaction ticket sent from the cashier terminal terminal.
     * Automatically binds the record to the active thread's multi-tenant ID context boundaries.
     */
    @PostMapping("/order")
    public ResponseEntity<?> processOrder(@RequestBody OrderRequest request) {
        // 1. Resolve current active multi-tenant boundary constraint
        String activeTenantId = TenantContext.getCurrentTenant();

        // 2. Instantiate and map the Master Order record
        Order order = new Order();
        order.setTenantId(activeTenantId); // Injected dynamically from middleware interceptor
        order.setCashierId(request.getCashierId());
        order.setWaiterId(request.getWaiterId());
        order.setTotalAmount(request.getTotalAmount());
        // Save header metadata to MySQL to generate our unique Order ID auto-increment key
        Order savedOrder = orderRepository.save(order);

        // 3. Loop through and persist individual line item breakdown metrics
        for (OrderRequest.ItemDetails item : request.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(savedOrder.getId());
            orderItem.setItemId(item.getItemId());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setUnitPrice(item.getUnitPrice());

            orderItemRepository.save(orderItem);
        }

        // 4. Return success map envelope response payload back to client interface
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Transaction saved successfully across tenant boundaries.");
        response.put("orderId", savedOrder.getId());

        return ResponseEntity.ok(response);
    }
}