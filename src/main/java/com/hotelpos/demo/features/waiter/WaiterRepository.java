package com.hotelpos.demo.features.waiter;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WaiterRepository extends JpaRepository<Waiter, Integer> {

    // Fetch only the active waiters working shifts inside a specific restaurant
    List<Waiter> findByTenantIdAndActiveTrue(String tenantId);
}