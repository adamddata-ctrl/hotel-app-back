package com.hotelpos.demo.features.checkout;

import com.hotelpos.demo.features.analytics.SalesSummaryResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional; // Added: Required for safe result handling wrapping containers

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    // Keep your excellent original tenant finder method safely intact!
    List<Order> findByTenantId(String tenantId);
    /*** 🔥 THE ANALYTICS ENGINE: Offloads math calculations directly to MySQL using composite indexing,
     * ensuring zero-lag reporting metrics restricted to the active tenant workspace boundaries.
     */
    @Query("SELECT new com.hotelpos.demo.features.analytics.SalesSummaryResponse(" +
            "SUM(o.totalAmount), " +
            "COUNT(o.id), " +
            "AVG(o.totalAmount)) " +
            "FROM Order o WHERE o.tenantId = :tenantId")
    Optional<SalesSummaryResponse> getSalesSummaryByTenant(@Param("tenantId") String tenantId);


    /**
     * 🔥 ADDED: Pulls daily sales data points along with their specific month groupings.
     * Generates individual daily bars for each month of the business year! [3.1]
     */
    @Query(value = "SELECT DATE_FORMAT(o.created_at, '%b %d') as dayLabel, " +
            "MONTH(o.created_at) as monthNum, " +
            "SUM(o.total_amount) as dailyTotal " +
            "FROM orders o WHERE o.tenant_id = :tenantId AND o.status = 'COMPLETED' " +
            "GROUP BY DATE(o.created_at), MONTH(o.created_at), DATE_FORMAT(o.created_at, '%b %d') " +
            "ORDER BY MONTH(o.created_at) ASC, DATE(o.created_at) ASC", nativeQuery = true)
    List<Object[]> findDailySalesMetricsGroupedByMonth(@Param("tenantId") String tenantId);

}