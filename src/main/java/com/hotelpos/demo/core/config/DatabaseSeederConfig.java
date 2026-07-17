package com.hotelpos.demo.core.config;

import com.hotelpos.demo.features.menu.MenuItem;
import com.hotelpos.demo.features.menu.MenuItemRepository;
import com.hotelpos.demo.features.auth.User;
import com.hotelpos.demo.features.auth.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class DatabaseSeederConfig {

    @Bean

    public CommandLineRunner seedDatabase(MenuItemRepository menuItemRepository, UserRepository userRepository) {
        return args -> {

            // =========================================================================
            // 1. ISOLATED MULTI-TENANT MENU CATALOG INITIALIZATION SEEDER
            // =========================================================================

            // --- HOTEL WORKSPACE ALPHA (Tenant ID: alpha-resort) ---
            String tenantAlpha = "alpha-resort";
            if (menuItemRepository.findByTenantId(tenantAlpha).isEmpty()) {
                System.out.println("🌱 SEED ENGINE: Pre-populating empty menu records for Tenant: " + tenantAlpha);

                MenuItem chicken = new MenuItem();
                chicken.setTenantId(tenantAlpha);
                chicken.setItemName("Crispy Fried Chicken");
                chicken.setCategory(MenuItem.Category.FOOD);
                chicken.setPrice(new BigDecimal("12.50"));
                MenuItem burger = new MenuItem();
                burger.setTenantId(tenantAlpha);
                burger.setItemName("Gourmet Beef Burger");
                burger.setCategory(MenuItem.Category.FOOD);
                burger.setPrice(new BigDecimal("14.00"));

                menuItemRepository.saveAll(List.of(chicken, burger));
            }

            // --- HOTEL WORKSPACE BETA (Tenant ID: beta-luxury) ---
            String tenantBeta = "beta-luxury";
            if (menuItemRepository.findByTenantId(tenantBeta).isEmpty()) {
                System.out.println("🌱 SEED ENGINE: Pre-populating empty menu records for Tenant: " + tenantBeta);

                MenuItem steak = new MenuItem();
                steak.setTenantId(tenantBeta);
                steak.setItemName("Premium Ribeye Steak");
                steak.setCategory(MenuItem.Category.FOOD);
                steak.setPrice(new BigDecimal("28.00"));

                MenuItem soda = new MenuItem();
                soda.setTenantId(tenantBeta);
                soda.setItemName("Classic Soft Drink");
                soda.setCategory(MenuItem.Category.DRINK);
                soda.setPrice(new BigDecimal("2.50"));

                menuItemRepository.saveAll(List.of(steak, soda));
            }

            // =========================================================================
            // 2. MULTI-TENANT AUTHENTICATION PROFILE INITIALIZATION SEEDER
            // =========================================================================

            // FIXED: Switched loop to an OR (||) check to guarantee persistent profile stability
            if (userRepository.findByTenantIdAndPinCode(tenantAlpha, "1111").isEmpty() ||  userRepository.findByTenantIdAndPinCode(tenantAlpha, "2222").isEmpty()) {

                System.out.println("🌱 SEED ENGINE: Instantiating multi-tenant waiter and owner rosters...");

                // Staff Profile #1: Front Counter Cashier User
                User waiterOne = new User();
                waiterOne.setId(java.util.UUID.randomUUID().toString());
                waiterOne.setTenantId(tenantAlpha);
                waiterOne.setUsername("Samuel Kebede");
                waiterOne.setPinCode("1111");
                waiterOne.setRole(User.Role.CASHIER); // Correctly locked to frontline sales terminal operations

                // Staff Profile #2: Higher Level Manager User
                User waiterTwo = new User();
                waiterTwo.setId(java.util.UUID.randomUUID().toString());
                waiterTwo.setTenantId(tenantAlpha);
                waiterTwo.setUsername("Helen Alemu");
                waiterTwo.setPinCode("2222");
                waiterTwo.setRole(User.Role.OWNER); // FIXED: Elevated role mapping to securely load your dashboard charts

                userRepository.saveAll(List.of(waiterOne, waiterTwo));
                System.out.println("🎯 SEED ENGINE: Baseline user rosters successfully synchronized.");
            } else {
                System.out.println("📊 SEED ENGINE: Staff user profiles verified. Seeding skipped.");
            }
        };
    }
}