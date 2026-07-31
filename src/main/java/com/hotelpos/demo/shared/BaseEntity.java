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
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = String.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public abstract class BaseEntity {

    @Column(name = "tenant_id", nullable = false, updatable = false, length = 36)
    private String tenantId;

    @PrePersist
    public void onPrePersist() {
        String currentTenant = TenantContext.getCurrentTenant();
        if (currentTenant == null || currentTenant.trim().isEmpty()) {
            // 🔥 CRITICAL FIX: Throw exception instead of saving to "DEFAULT_TENANT_DEV"
            throw new IllegalStateException("Cannot persist entity without a valid tenant context.");
        }
         this.tenantId = currentTenant;
    }
}