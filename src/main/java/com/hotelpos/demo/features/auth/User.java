package com.hotelpos.demo.features.auth;

import com.hotelpos.demo.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Shared Multi-Tenant User Entity Account. Extends BaseEntity to automatically
 * inherit the required tenant_id partitioning constraint columns.
 */
@Entity
@Table(
        name = "users",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "username"})
)
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {

    @Id
    @Column(length = 36)
    private String id; // Pre-generated UUID string token identifier

    @Column(nullable = false, length = 50)
    private String username; // Access name or handle unique within individual tenant spaces

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private Role role; // Access boundaries: OWNER or CASHIER

    @Column(name = "pin_code", length = 4)
    private String pinCode; // High-speed 4-digit PIN token used exclusively by Cashiers

    @Column(length = 255)
    private String password; // Securely hashed bCrypt password configuration used by Owners

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName; // The descriptive readable name of the individual user

    /**
     * Enumerator tracking system-wide role authorization tiers.
     */
    public enum Role {
        OWNER,
        CASHIER
    }
}