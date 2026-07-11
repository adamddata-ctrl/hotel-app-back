package com.hotelpos.demo.features.analytics;

import com.hotelpos.demo.core.tenant.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @PersistenceContext
private EntityManager entityManager;

    /**
     * Aggregates total business operational revenue metrics for a specific targeted date
     * filtered safely by active tenant contexts.
     */
    @GetMapping("/daily-summary")
    public ResponseEntity<?> getDailySummary(@RequestParam("date") String dateStr) {
        String activeTenantId = TenantContext.getCurrentTenant();
        LocalDate targetDate = LocalDate.parse(dateStr);

        // Calculate total daily cash intake volume
        String revenueQuery = "SELECT COALESCE(SUM(o.total_amount), 0) FROM Order o " +
                "WHERE o.tenantId = :tenantId AND DATE(o.createdAt) = :targetDate";

        // Count aggregate checkout transaction execution loops
        String countsQuery = "SELECT COUNT(o) FROM Order o " +
                "WHERE o.tenantId = :tenantId AND DATE(o.createdAt) = :targetDate";

        Object revenue = entityManager.createQuery(revenueQuery)
                .setParameter("tenantId", activeTenantId)
                .setParameter("targetDate", targetDate)
                .getSingleResult();

        Object orderCount = entityManager.createQuery(countsQuery)
                .setParameter("tenantId", activeTenantId)
                .setParameter("targetDate", targetDate)
                .getSingleResult();

        Map<String, Object> summary = new HashMap<>();
        summary.put("date", dateStr);
        summary.put("totalRevenue", revenue);
        summary.put("totalOrders", orderCount);
        return ResponseEntity.ok(summary);
    }

    /**
     * Maps overall waiter throughput logs for any targeted monthly calendar boundary parameters.
     */
    @GetMapping("/waiter-performance")
    public ResponseEntity<List<WaiterPerformanceReport>> getWaiterMonthlyPerformance(
            @RequestParam("year") int year, @RequestParam("month") int month) {

        String activeTenantId = TenantContext.getCurrentTenant();

        String hql = "SELECT new com.hotelpos.demo.features.analytics.WaiterPerformanceReport(" +
                "w.waiterName, COUNT(o.id), SUM(o.totalAmount)) " +
                "FROM Order o JOIN Waiter w ON o.waiterId = w.id " +
                "WHERE o.tenantId = :tenantId " +
                "AND YEAR(o.createdAt) = :year AND MONTH(o.createdAt) = :month " +
                "GROUP BY w.waiterName ORDER BY SUM(o.totalAmount) DESC";

        List<WaiterPerformanceReport> report = entityManager.createQuery(hql, WaiterPerformanceReport.class)
                .setParameter("tenantId", activeTenantId)
                .setParameter("year", year)
                .setParameter("month", month)
                .getResultList();

        return ResponseEntity.ok(report);
    }

    /**
     * Resolves individual menu tracking metrics sorted descending by absolute transaction quantity velocity.
     */
    @GetMapping("/menu-popularity")
    public ResponseEntity<List<ItemSalesVolumeReport>> getMenuPopularityMetrics(
            @RequestParam("year") int year, @RequestParam("month") int month) {

        String activeTenantId = TenantContext.getCurrentTenant();

        String hql = "SELECT new com.hotelpos.demo.features.analytics.ItemSalesVolumeReport(" +
                "mi.itemName, STR(mi.category), SUM(oi.quantity), SUM(oi.quantity * oi.unitPrice)) " +
                "FROM OrderItem oi " +
                "JOIN Order o ON oi.orderId = o.id " +
                "JOIN MenuItem mi ON oi.itemId = mi.id " +
                "WHERE o.tenantId = :tenantId " +
                "AND YEAR(o.createdAt) = :year AND MONTH(o.createdAt) = :month " +
                "GROUP BY mi.itemName, mi.category ORDER BY SUM(oi.quantity) DESC";

        List<ItemSalesVolumeReport> report = entityManager.createQuery(hql, ItemSalesVolumeReport.class)
                .setParameter("tenantId", activeTenantId)
                .setParameter("year", year)
                .setParameter("month", month)
                .getResultList();

        return ResponseEntity.ok(report);
    }
}
