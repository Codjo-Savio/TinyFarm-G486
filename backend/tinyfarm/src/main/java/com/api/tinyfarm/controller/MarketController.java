package com.api.tinyfarm.controller;

import java.util.List;
import com.api.tinyfarm.model.Market;
import com.api.tinyfarm.service.MarketService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/market")
public class MarketController {

    private final MarketService marketService;

    public MarketController(MarketService marketService) {
        this.marketService = marketService;
    }

    // Endpoint utilise par le frontend pour afficher toutes les offres du marche.
    @GetMapping("")
    public ResponseEntity<List<Market>> getAll() {
        try {
            return ResponseEntity.ok(marketService.findAll());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<Market> getByUserId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(marketService.findByUserId(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/product/{productId}")
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

    @GetMapping("/quantity/{quantity}")
    public ResponseEntity<Market> getByQuantity(@PathVariable int quantity) {
        try {
            return ResponseEntity.ok(marketService.findByQuantity(quantity));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("")
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
}
