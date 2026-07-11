package com.hotelpos.demo.features.checkout;

import com.hotelpos.demo.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor

public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // Unique auto-increment order number

    @Column(name = "cashier_id", nullable = false, length = 36)
    private String cashierId; // Links transaction directly to the logged-in cashier user UUID

    @Column(name = "waiter_id", nullable = false)
    private Integer waiterId; // Links directly to the waiter who brought the order slip

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount; // Final calculated total due for the ticket

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now(); // Explicit timestamp for daily/monthly analytics
}