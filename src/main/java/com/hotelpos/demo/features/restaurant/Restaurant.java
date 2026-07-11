package com.hotelpos.demo.features.restaurant;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * Master Enterprise Tenant Model. Tracks independent hotel businesses
 * registered to lease the SaaS software platform instance.
 */
@Entity
@Table(name = "restaurants")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Restaurant {

    @Id
    @Column(length = 36)
    private String id; // Pre-generated UUID string token identifier

    @Column(nullable = false, length = 100)
    private String name; // Corporate legal operating trading name

    @Column(name = "owner_email", nullable = false, unique = true, length = 100)
    private String ownerEmail; // Principal account verification address

    @Column(name = "is_active", nullable = false)
    private boolean active = true; // Billing flag; false locks cashiers out

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}