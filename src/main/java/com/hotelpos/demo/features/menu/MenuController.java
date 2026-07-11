package com.hotelpos.demo.features.menu;

import com.hotelpos.demo.core.tenant.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
@CrossOrigin(origins = "http://localhost:4200")
public class MenuController {

    @Autowired
    private MenuItemRepository menuItemRepository;
    /**
     * Resolves the entire food and drink menu catalogs for the checkout screen matrix grid.
     * Injected interceptors automatically pass down tenant parameters.
     */
    @GetMapping("/all")
    public ResponseEntity<List<MenuItem>> getMenuCatalogByTenant() {
        String activeTenantId = TenantContext.getCurrentTenant();

        // Pull all food and drink rows matching the current hotel tenant
        List<MenuItem> catalog = menuItemRepository.findAll().stream()
                .filter(item -> activeTenantId.equals(item.getTenantId()))
                .toList();

        return ResponseEntity.ok(catalog);
    }
}