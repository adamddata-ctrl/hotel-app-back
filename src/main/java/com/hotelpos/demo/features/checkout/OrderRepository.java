package com.hotelpos.demo.features.checkout;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    // Fetch all transaction records scoped strictly to an individual restaurant client
    List<Order> findByTenantId(String tenantId);
}