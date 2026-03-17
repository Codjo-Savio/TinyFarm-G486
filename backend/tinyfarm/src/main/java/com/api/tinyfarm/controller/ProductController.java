package com.api.tinyfarm.controller;

import com.api.tinyfarm.model.Product;
import com.api.tinyfarm.service.ProductService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAll() {
        return ResponseEntity.ok(productService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Product> create(@RequestBody Product product) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            productService.add(product)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> update(
        @PathVariable Long id,
        @RequestBody Product product
    ) {
        return ResponseEntity.ok(productService.update(id, product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/all")
    public ResponseEntity<Void> deleteAll() {
        productService.deleteAllProducts();
        return ResponseEntity.noContent().build();
    }

    // --- Custom Filters ---

    @GetMapping("/filter/collectible/{collectible}")
    public ResponseEntity<List<Product>> getByCollectible(
        @PathVariable Boolean collectible
    ) {
        return ResponseEntity.ok(productService.findByCollectible(collectible));
    }

    @GetMapping("/filter/coefficient/{coefficient}")
    public ResponseEntity<List<Product>> getByCoefficient(
        @PathVariable Integer coefficient
    ) {
        return ResponseEntity.ok(productService.findByCoefficient(coefficient));
    }
}
