package com.api.tinyfarm.controller;

import java.util.HashMap;

import com.api.tinyfarm.model.Cooperative;
import com.api.tinyfarm.model.Product;
import com.api.tinyfarm.service.CooperativeService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.method.P;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cooperative")
public class CooperativeController {

    private final CooperativeService cooperativeService;

    public CooperativeController(CooperativeService cooperativeService) {
        this.cooperativeService = cooperativeService;
    }

    @GetMapping
    public ResponseEntity<HashMap<Long, Float>> getAll() {
        try {
            HashMap<Long, Float> productPrices = cooperativeService.getAvailableProducts();
            return ResponseEntity.ok(productPrices);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{idBuyer}/{description}")
    public ResponseEntity<Integer> deleteByDescription(@PathVariable Long idBuyer, @PathVariable String description) {
        try {
            cooperativeService.deleteLessExpensiveWithDescription(idBuyer, description);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
