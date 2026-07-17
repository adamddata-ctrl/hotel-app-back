package com.hotelpos.demo.shared;

import com.hotelpos.demo.core.tenant.TenantContext;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Data;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

@Data
@MappedSuperclass
// 1. Define a global tenant parameter structure
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = String.class))
// 2. Instruct Hibernate to append this WHERE condition automatically to SELECT/UPDATE/DELETE actions
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public abstract class BaseEntity {

    @Column(name = "tenant_id", nullable = false, updatable = false, length = 36)
    private String tenantId;

    @PrePersist
    public void onPrePersist() {
        String currentTenant = TenantContext.getCurrentTenant();
        if (currentTenant == null || currentTenant.trim().isEmpty()) {
            this.tenantId = "DEFAULT_TENANT_DEV";
        } else {
            this.tenantId = currentTenant;
        }
    }
}