package com.hotelpos.demo.features.checkout;

import com.hotelpos.demo.features.analytics.ItemSalesVolumeReport;
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
    @Query("SELECT new com.hotelpos.demo.features.analytics.SalesSummaryResponse(" +
            "COALESCE(SUM(o.totalAmount), 0.0), " +  // 💡 Safe fallback for NULL
            "COUNT(o.id), " +
            "COALESCE(AVG(o.totalAmount), 0.0)) " +  // 💡 Safe fallback for NULL
            "FROM Order o WHERE o.tenantId = :tenantId")
    Optional<SalesSummaryResponse> getSalesSummaryByTenant(@Param("tenantId") String tenantId);

    // 👇 YOUR EXISTING DAILY METRICS QUERY FIXED WITH COALESCE
    @Query(value = "SELECT DATE_FORMAT(o.created_at, '%b %d') as dayLabel, " +
            "MONTH(o.created_at) as monthNum, " +
            "COALESCE(SUM(o.total_amount), 0.0) as dailyTotal " + // 💡 Safe fallback for NULL
            "FROM orders o WHERE o.tenant_id = :tenantId AND o.status = 'COMPLETED' " +
            "GROUP BY DATE(o.created_at), MONTH(o.created_at), DATE_FORMAT(o.created_at, '%b %d') " +
            "ORDER BY MONTH(o.created_at) ASC, DATE(o.created_at) ASC", nativeQuery = true)
    List<Object[]> findDailySalesMetricsGroupedByMonth(@Param("tenantId") String tenantId);

    // 👇 ADD OR UPDATE YOUR MENU POPULARITY METHOD DIRECTLY BELOW LINE 35 LIKE THIS:
    @Query(value = "SELECT mi.item_name as itemName, " +
            "CAST(mi.category AS CHAR) as category, " +
            "COALESCE(SUM(oi.quantity), 0) as totalQuantitySold, " +
            "COALESCE(SUM(oi.quantity * mi.price), 0.0) as totalRevenueGenerated " +
            "FROM order_items oi " +
            "JOIN orders o ON oi.order_id = o.id " +
            "JOIN menu_items mi ON oi.item_id = mi.id " +
            "WHERE o.tenant_id = :tenantId " +
            "GROUP BY mi.item_name, mi.category " +
            "ORDER BY SUM(oi.quantity) DESC", nativeQuery = true)
    List<Object[]> findMenuPopularityNative(@Param("tenantId") String tenantId);
}

