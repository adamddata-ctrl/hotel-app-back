package com.hotelpos.demo.core.config;

import com.hotelpos.demo.features.menu.MenuItem;
import com.hotelpos.demo.features.menu.MenuItemRepository;
import com.hotelpos.demo.features.auth.User;
import com.hotelpos.demo.features.auth.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class DatabaseSeederConfig {

    @Bean
    public CommandLineRunner seedDatabase(
            MenuItemRepository menuItemRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            // =========================================================================
            // GLOBAL WORKSPACE CONFIGURATION ALIGNMENT
            // =========================================================================
            String tenantAlpha = "DEFAULT_TENANT_DEV";
            String tenantBeta = "beta-luxury";

            // =========================================================================
            // 1. MULTI-TENANT MENU CATALOG INITIALIZATION SEEDER
            // =========================================================================
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

            // =========================================================================
            // 2. MULTI-TENANT AUTHENTICATION PROFILE INITIALIZATION SEEDER
            // =========================================================================
            // FIXED: Check by unique usernames to stop the duplicate entry SQL crash
            if (!userRepository.existsByUsername("Samuel Kebede") && !userRepository.existsByUsername("Helen Alemu")) {

                System.out.println("🌱 SEED ENGINE: Instantiating multi-tenant waiter and owner rosters...");

                // Staff Profile #1: Front Counter Cashier User
                User waiterOne = new User();
                waiterOne.setId("USER-DEV-SAMUEL-1111"); // Fixed static ID stops primary key collisions on reload
                waiterOne.setTenantId(tenantAlpha);
                waiterOne.setUsername("Samuel Kebede");
                waiterOne.setPinCode(passwordEncoder.encode("1111"));
                waiterOne.setRole(User.Role.CASHIER);

                // Staff Profile #2: Higher Level Manager/Owner User
                User waiterTwo = new User();
                waiterTwo.setId("USER-DEV-HELEN-2222");  // Fixed static ID stops primary key collisions on reload
                waiterTwo.setTenantId(tenantAlpha);
                waiterTwo.setUsername("Helen Alemu");
                waiterTwo.setPinCode(passwordEncoder.encode("2222"));
                waiterTwo.setRole(User.Role.OWNER);

                userRepository.saveAll(List.of(waiterOne, waiterTwo));
                System.out.println("🌱 SEED ENGINE: Baseline user rosters successfully synchronized.");

            } else {
                System.out.println("🌱 SEED ENGINE: Staff user profiles verified. Seeding skipped.");
            }
        };
    }
}
