package com.hotelpos.demo.shared;
import com.hotelpos.demo.core.tenant.TenantContext;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Data;
@Data
@MappedSuperclass
public abstract class BaseEntity {

    @Column(name = "tenant_id", nullable = false, updatable = false, length = 36)
    private String tenantId;
    /**
     * 🔥 AUTOMATIC LIFECYCLE HOOK: Intercepts database saves (inserts)
     * and injects the current ThreadLocal tenant context right before executing SQL.*/
    @PrePersist
    public void onPrePersist() {
        String currentTenant = TenantContext.getCurrentTenant();
        // Safety validation: Default fallback strategy if a request executes out-of-context
        if (currentTenant == null || currentTenant.trim().isEmpty()) {
            this.tenantId = "DEFAULT_TENANT_DEV";
        } else {
            this.tenantId = currentTenant;
        }
    }
}