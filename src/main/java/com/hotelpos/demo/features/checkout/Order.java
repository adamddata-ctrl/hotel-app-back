package com.hotelpos.demo.features.checkout;
import com.hotelpos.demo.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders")
public class Order extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "cashier_id", nullable = false, length = 36)
    private String cashierId;

    @Column(name = "waiter_id", nullable = false)
    private Integer waiterId;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    /*** 🔥 THE RELATION LINK: cascade = CascadeType.ALL means saving this Order
     * automatically saves every individual OrderItem in the array list inside MyS*/
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();
    @Override
    public void onPrePersist() {
        super.onPrePersist(); // Injects your automated tenantId firewall assignment hook cleanly
        this.createdAt = LocalDateTime.now();
    }
}