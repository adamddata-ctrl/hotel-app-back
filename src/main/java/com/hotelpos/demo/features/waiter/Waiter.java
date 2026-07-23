package com.hotelpos.demo.features.waiter;
import com.fasterxml.jackson.annotation.JsonProperty; // 1. Add this import statement
import com.hotelpos.demo.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "waiters")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Waiter extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // Standard auto-incrementing ID integer for fast relational index mapping

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "waiter_name", nullable = false, length = 100)
    @JsonProperty("waiterName") // 2. Force the JSON key serialization to match your Angular variable
    private String waiterName; // The readable name of the service staff member

    @Column(name = "is_active", nullable = false)
    private boolean active = true; // Status toggle to handle active shifts or termination
}