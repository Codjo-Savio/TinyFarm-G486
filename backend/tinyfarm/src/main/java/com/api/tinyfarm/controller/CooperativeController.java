package com.api.tinyfarm.controller;

import java.util.HashMap;

import com.api.tinyfarm.dto.CooperativeSaleRequest;
import com.api.tinyfarm.service.CooperativeService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


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

    @GetMapping("/isOpen")
    public ResponseEntity<Boolean> getIsOpen() {
        try {
            return ResponseEntity.ok(cooperativeService.isOpen());
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

    @PostMapping("/sell")
    public ResponseEntity<Float> sellToCooperative(
        @RequestBody CooperativeSaleRequest request
    ) {
        try {
            Float total = cooperativeService.sellToCooperative(
                request.getSellerId(),
                request.getProductId(),
                request.getQuantity()
            );
            return ResponseEntity.ok(total);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
