package com.hotelpos.demo.features.checkout;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {

    // Find all item rows belonging to a single printed cashier ticket
    List<OrderItem> findByOrderId(Integer orderId);
}