package com.api.tinyfarm.controller;

import com.api.tinyfarm.model.Market;
import com.api.tinyfarm.service.MarketService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/market")
public class MarketController {

    private final MarketService marketService;

    public MarketController(MarketService marketService) {
        this.marketService = marketService;
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<Market> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(marketService.findById(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/productId/{productId}")
    public ResponseEntity<Market> getByProductId(@PathVariable Long productId) {
        try {
            return ResponseEntity.ok(marketService.findByProductId(productId));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/price/{price}")
    public ResponseEntity<Market> getByPrice(@PathVariable float price) {
        try {
            return ResponseEntity.ok(marketService.findByPrice(price));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Market> create(@RequestBody Market market) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(
                marketService.create(market)
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            return ResponseEntity.status(
                HttpStatus.INTERNAL_SERVER_ERROR
            ).build();
        }
    }

    @PutMapping("/id/{id}")
    public ResponseEntity<Market> update(
        @PathVariable Long id,
        @RequestBody Market market
    ) {
        try {
            return ResponseEntity.ok(marketService.update(id, market));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{userId}/{productId}")
    public ResponseEntity<Void> deleteProductById(
        @PathVariable Long userId,
        @PathVariable Long productId
    ) {
        try {
            marketService.deleteProductById(userId, productId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/id/{uid}")
    public ResponseEntity<Void> deleteById(@PathVariable Long uid) {
        try {
            marketService.deleteByID(uid);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAll() {
        try {
            marketService.deleteAll();
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(
                HttpStatus.INTERNAL_SERVER_ERROR
            ).build();
        }
    }
}
