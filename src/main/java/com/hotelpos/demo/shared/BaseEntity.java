package com.hotelpos.demo.shared;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;

/**
 * Shared entity superclass to automatically distribute multitenant data isolation
 * rules across all downstream relational operational data tables.
 */
@Data
@MappedSuperclass
public abstract class BaseEntity {

    @Column(name = "tenant_id", nullable = false, updatable = false, length = 36)
    private String tenantId;
}