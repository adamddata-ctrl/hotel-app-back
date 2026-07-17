package com.hotelpos.demo.features.analytics;

import com.hotelpos.demo.core.tenant.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.hotelpos.demo.features.checkout.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics") // Fixed: Clean, absolute base route directory
@CrossOrigin(origins = "http://localhost:4200") // Prevents browser CORS blocks
public class AnalyticsController {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Aggregates total business operational revenue metrics for a specific targeted date.
     */
    @GetMapping("/daily-summary") // Fixed: Streamlined mapping route
    public ResponseEntity<?> getDailySummary(@RequestParam("date") String dateStr) {
        String activeTenantId = TenantContext.getCurrentTenant();
        LocalDate targetDate = LocalDate.parse(dateStr);
        // Define clean day-window boundaries to isolate dates securely without SQL function leaks
        LocalDateTime startOfDay = targetDate.atStartOfDay();
        LocalDateTime endOfDay = targetDate.atTime(LocalTime.MAX);

        // Fixed: Swapped table snake_case names for proper camelCase Java entity field variables
        String revenueQuery = "SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o " +
                "WHERE o.tenantId = :tenantId AND o.createdAt BETWEEN :start AND :end";

        String countQuery = "SELECT COUNT(o.id) FROM Order o " +
                "WHERE o.tenantId = :tenantId AND o.createdAt BETWEEN :start AND :end";

        Object revenue = entityManager.createQuery(revenueQuery)
                .setParameter("tenantId", activeTenantId)
                .setParameter("start", startOfDay)
                .setParameter("end", endOfDay)
                .getSingleResult();
        Object orderCount = entityManager.createQuery(countQuery)
                .setParameter("tenantId", activeTenantId)
                .setParameter("start", startOfDay)
                .setParameter("end", endOfDay)
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
    @GetMapping("/waiter-performance") // Fixed: Cleaned routing path layout
    public ResponseEntity<List<WaiterPerformanceReport>> getWaiterMonthlyPerformance(
            @RequestParam("year") int year, @RequestParam("month") int month) {

        String activeTenantId = TenantContext.getCurrentTenant();

        // Fixed: Aligned unmapped entity cross-joins using property-matching criteria
        String hql = "SELECT new com.hotelpos.demo.features.analytics.WaiterPerformanceReport(" +
                "w.waiterName, COUNT(o.id), SUM(o.totalAmount)) " +
                "FROM Order o, Waiter w WHERE o.waiterId = w.id " +
                "AND o.tenantId = :tenantId " +
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
     * Resolves individual menu tracking metrics sorted descending by transaction quantity velocity.
     */
    @GetMapping("/menu-popularity") // Fixed: Cleaned path routing string format
    public ResponseEntity<List<ItemSalesVolumeReport>> getMenuPopularityMetrics(
            @RequestParam("year") int year, @RequestParam("month") int month) {

        String activeTenantId = TenantContext.getCurrentTenant();
        // Fixed: Standardized child relation joins matching your object mappings (oi.order and oi.itemId)
        String hql = """
            SELECT new com.hotelpos.demo.features.analytics.ItemSalesVolumeReport(
                mi.itemName, mi.category, SUM(oi.quantity), SUM(oi.quantity * oi.unitPrice)
            )
            FROM OrderItem oi 
            JOIN oi.order o 
            JOIN MenuItem mi ON oi.itemId = mi.id
            WHERE o.tenantId = :tenantId
            AND YEAR(o.createdAt) = :year 
            AND MONTH(o.createdAt) = :month
            GROUP BY mi.itemName, mi.category 
            ORDER BY SUM(oi.quantity) DESC
            """;

        List<ItemSalesVolumeReport> report = entityManager.createQuery(hql, ItemSalesVolumeReport.class)
                .setParameter("tenantId", activeTenantId)
                .setParameter("year", year)
                .setParameter("month", month)
                .getResultList();

        return ResponseEntity.ok(report);
    }




    @Autowired




    private OrderRepository orderRepository; // 🔥 FIXED: Simplified type path declaration
   // private com.hotelpos.demo.features.checkout.OrderRepository orderRepository; // Injected to access transaction records

    /**
     * 🔥 ADDED: Streams comprehensive daily transaction records mapped out by month numerical values [3.1].
     */
    @GetMapping("/daily-month-bars")
    public ResponseEntity<?> getDailySalesBarsByMonth() {
        // Pulls multi-tenant ID context securely from thread headers [3.1]
        String currentTenantId = com.hotelpos.demo.core.tenant.TenantContext.getCurrentTenant();
        List<Object[]> rawData = orderRepository.findDailySalesMetricsGroupedByMonth(currentTenantId);

        List<ChartDataDTO> formattedList = new java.util.ArrayList<>();
        for (Object[] row : rawData) {
            String timeLabel = (String) row[0];
            int monthNum = ((Number) row[1]).intValue();
            double salesTotal = ((Number) row[2]).doubleValue();

            formattedList.add(new ChartDataDTO(timeLabel, monthNum, salesTotal));
        }
        return ResponseEntity.ok(formattedList);

    }





    /**
     * Aggregates and returns the total consolidated gross revenue for the requesting restaurant workspace.
     * Enforces strict multi-tenant context boundary constraints during financial calculation.
     */
    @GetMapping("/total-revenue")
    public ResponseEntity<?> calculateConsolidatedGrossRevenue() {
        try {
            // 1. Fetch the active tenant workspace ID securely from the thread context firewall
            String activeTenantId = com.hotelpos.demo.core.tenant.TenantContext.getCurrentTenant();

            // 2. Stream and filter transaction documents belonging strictly to this restaurant instance
            java.math.BigDecimal grandTotalAccumulator = orderRepository.findByTenantId(activeTenantId)
                    .stream()
                    .flatMap(order -> order.getItems().stream()) // Flatten the items list arrays safely
                    .map(item -> {
                        java.math.BigDecimal itemQty = java.math.BigDecimal.valueOf(item.getQuantity());
                        java.math.BigDecimal unitPrice = item.getUnitPrice();

                        // Defend against null valuation parameters in the database rows
                        if (unitPrice == null) {
                            unitPrice = java.math.BigDecimal.ZERO;
                        }

                        return unitPrice.multiply(itemQty); // Calculate row totals atomically
                    })
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add); // Sum all rows together cleanly

            // 3. Return a verified analytics packet structure matching your frontend scoring cards
            java.util.Map<String, Object> financialMetrics = new java.util.HashMap<>();
            financialMetrics.put("tenantId", activeTenantId);
            financialMetrics.put("totalGrossRevenue", grandTotalAccumulator);
            financialMetrics.put("reconciledAt", java.time.LocalDateTime.now().toString());

            return ResponseEntity.ok(financialMetrics);

        } catch (Exception ex) {
            return ResponseEntity.status(500).body(java.util.Map.of(
                    "error", "Financial ledger consolidation compilation processing breakdown.",
                    "details", ex.getMessage()
            ));
        }
    }
}










