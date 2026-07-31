package com.hotelpos.demo.features.restaurant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants") // 🔥 FIXED: Added "/api" prefix to match your frontend environment.ts
public class RestaurantController {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @GetMapping("/list")
    public ResponseEntity<List<Restaurant>> getAllActiveTenants() {
        return ResponseEntity.ok(restaurantRepository.findAll());
    }
}