package com.api.tinyfarm.controller;

import com.api.tinyfarm.model.Cooperative;
import com.api.tinyfarm.model.Product;
import com.api.tinyfarm.service.CooperativeService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<List<Product>> getAll() {
        try {
            List<Product> products = cooperativeService.getAvailableProducts();
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
