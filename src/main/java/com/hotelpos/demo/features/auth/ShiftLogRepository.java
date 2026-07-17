package com.hotelpos.demo.features.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface ShiftLogRepository extends JpaRepository<ShiftLog, Long> {

    // Locates an active, running shift for an employee that doesn't have a clock-out time yet [3.1]
    @Query("SELECT s FROM ShiftLog s WHERE s.cashierId = :cashierId AND s.clockOutTime IS NULL AND s.tenantId = :tenantId")
    Optional<ShiftLog> findActiveShift(@Param("cashierId") String cashierId, @Param("tenantId") String tenantId);
}