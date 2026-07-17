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
    /**
     * Executes automatically upon application startup to pre-seed the database
     * with isolated, tenant-specific menu items and staff structures [3.1].
     */
    @Bean
    public CommandLineRunner seedDatabase(MenuItemRepository menuItemRepository, UserRepository userRepository) {
        return args -> {
            String testTenant = "DEFAULT_TENANT_DEV";

            // ==========================================================================
            // 🍔 1. MENU CONFIGURATIONS INITIALIZATION SEEDER
            // ==========================================================================
            if (menuItemRepository.findByTenantId(testTenant).isEmpty()) {
                System.out.println("🌱 SEED ENGINE: Pre-populating empty multi-tenant register menus...");

                MenuItem chicken = new MenuItem();
                chicken.setTenantId(testTenant);
                chicken.setItemName("Crispy Fried Chicken");
                chicken.setCategory(MenuItem.Category.FOOD);
                chicken.setPrice(new BigDecimal("12.50"));

                MenuItem burger = new MenuItem();
                burger.setTenantId(testTenant);
                burger.setItemName("Gourmet Beef Burger");
                burger.setCategory(MenuItem.Category.FOOD);
                burger.setPrice(new BigDecimal("14.00"));

                MenuItem soda = new MenuItem();
                soda.setTenantId(testTenant);
                soda.setItemName("Classic Soft Drink");
                soda.setCategory(MenuItem.Category.DRINK);
                soda.setPrice(new BigDecimal("2.50"));

                MenuItem coffee = new MenuItem();
                coffee.setTenantId(testTenant);
                coffee.setItemName("Espresso Macchiato");
                coffee.setCategory(MenuItem.Category.DRINK);
                coffee.setPrice(new BigDecimal("3.50"));

                menuItemRepository.saveAll(List.of(chicken, burger, soda, coffee));
                System.out.println("✅ SEED ENGINE: Baseline core menus successfully registered.");
            } else {
                System.out.println("⏭️ SEED ENGINE: Restaurant menu catalog records verified. Seeding skipped.");
            }

            // ==========================================================================
            // 🤵 2. FLOOR STAFF INITIALIZATION SEEDER
            // ==========================================================================
            // Safely verify if these specific default user profiles are missing for our dev tenant
            if (userRepository.findByTenantIdAndPinCode(testTenant, "1111").isEmpty() &&
                    userRepository.findByTenantIdAndPinCode(testTenant, "2222").isEmpty()) {

                System.out.println("🌱 SEED ENGINE: Instantiating automated multi-tenant waiter profiles...");

                // Staff Waiter #1 Profile Definition
                User waiterOne = new User();
                waiterOne.setId(java.util.UUID.randomUUID().toString()); // Assign required unique String ID
                waiterOne.setTenantId(testTenant);
                waiterOne.setUsername("Samuel Kebede"); // 🔥 Removed setDisplayName line
                waiterOne.setPinCode("1111"); // High-speed 4-digit PIN selection code
                waiterOne.setRole(User.Role.CASHIER); // Set to CASHIER/STAFF tier

                // Staff Waiter #2 Profile Definition
                User waiterTwo = new User();
                waiterTwo.setId(java.util.UUID.randomUUID().toString()); // Assign required unique String ID
                waiterTwo.setTenantId(testTenant);
                waiterTwo.setUsername("Helen Alemu"); // 🔥 Removed setDisplayName line
                waiterTwo.setPinCode("2222");
                waiterTwo.setRole(User.Role.CASHIER);
                // Commit both staff records to MySQL at once
                userRepository.saveAll(List.of(waiterOne, waiterTwo));
                System.out.println("✅ SEED ENGINE: Baseline waiter rosters successfully synchronized.");
            } else {
                System.out.println("⏭️ SEED ENGINE: Staff user profiles verified. User seeding skipped.");
            }
            };
    }
}