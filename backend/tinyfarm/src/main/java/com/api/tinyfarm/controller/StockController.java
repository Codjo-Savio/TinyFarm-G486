package com.api.tinyfarm.controller;

import com.api.tinyfarm.model.Stock;
import com.api.tinyfarm.service.StockService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/stocks")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping("")
    public ResponseEntity<List<Stock>> getAll() {
        try {
            return ResponseEntity.ok(stockService.findAll());
        } catch (Exception e) {
            return ResponseEntity.status(
                HttpStatus.INTERNAL_SERVER_ERROR
            ).build();
        }
    }

    @GetMapping("/user/{userId}/product/{productId}")
    public ResponseEntity<Stock> getById(
        @PathVariable Long userId,
        @PathVariable Long productId
    ) {
        try {
            return ResponseEntity.ok(stockService.findById(userId, productId));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Stock>> getByUser(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(stockService.findByUser(userId));
        } catch (Exception e) {
            return ResponseEntity.status(
                HttpStatus.INTERNAL_SERVER_ERROR
            ).build();
        }
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Stock>> getByProduct(
        @PathVariable Long productId
    ) {
        try {
            return ResponseEntity.ok(stockService.findByProduct(productId));
        } catch (Exception e) {
            return ResponseEntity.status(
                HttpStatus.INTERNAL_SERVER_ERROR
            ).build();
        }
    }

    @PostMapping("")
    public ResponseEntity<Stock> create(@RequestBody Stock stock) {
        try {
            Stock created = stockService.create(stock);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            return ResponseEntity.status(
                HttpStatus.INTERNAL_SERVER_ERROR
            ).build();
        }
    }

    @PutMapping("/user/{userId}/product/{productId}")
    public ResponseEntity<Stock> update(
        @PathVariable Long userId,
        @PathVariable Long productId,
        @RequestBody Stock stock
    ) {
        try {
            return ResponseEntity.ok(
                stockService.update(userId, productId, stock)
            );
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/user/{userId}/product/{productId}")
    public ResponseEntity<Void> delete(
        @PathVariable Long userId,
        @PathVariable Long productId
    ) {
        try {
            stockService.delete(userId, productId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Void> deleteByUser(@PathVariable Long userId) {
        try {
            stockService.deleteByUser(userId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(
                HttpStatus.INTERNAL_SERVER_ERROR
            ).build();
        }
    }

    @DeleteMapping("/product/{productId}")
    public ResponseEntity<Void> deleteByProduct(@PathVariable Long productId) {
        try {
            stockService.deleteByProduct(productId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(
                HttpStatus.INTERNAL_SERVER_ERROR
            ).build();
        }
    }
}
