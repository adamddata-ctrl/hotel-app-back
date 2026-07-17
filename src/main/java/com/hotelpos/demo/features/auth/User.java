package com.hotelpos.demo.features.auth;

import com.hotelpos.demo.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "users",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "username"}),
        indexes = {
                // 🔥 ADDED: Speeds up your multi-tenant 4-digit PIN touchscreen login query lookups! [3.1]
                @Index(name = "idx_tenant_pin_auth", columnList = "tenant_id, pin_code")
        }
)
public class User extends BaseEntity {

    @Id
    @Column(length = 36)
    private String id; // Pre-generated UUID string token identifier

    @Column(nullable = false, length = 50)
    private String username; // Access name or handle unique within individual tenant spaces

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, columnDefinition = "ENUM('OWNER', 'CASHIER')")
    private Role role; // Access boundaries: OWNER or CASHIER

    // FIXED: Increased length from 4 to 60 to fully accommodate secure BCrypt hashes!
    @Column(name = "pin_code", length = 60)
    private String pinCode;

    @Column(length = 255)
    private String password; // Securely hashed password credential token used by Owners

    /** Helper shortcut method to return a user profile display name text string layout */
    public String getDisplayName() {
        return this.username != null ? this.username.toUpperCase() : "UNKNOWN CASHIER";
    }

    /** System identity security access roles matrix boundaries schema definitions. */
    public enum Role {
        OWNER,
        CASHIER
    }
}