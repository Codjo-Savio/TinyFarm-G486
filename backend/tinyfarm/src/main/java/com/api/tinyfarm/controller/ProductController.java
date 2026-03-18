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

        try{
            return ResponseEntity.ok(productService.findAll());
        }catch(Exception e){
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<Product> getById(@PathVariable Long id) {
        try{
            return ResponseEntity.ok(productService.findById(id));
        }catch(Exception e){
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("")
    public ResponseEntity<Product> create(@RequestBody Product product) {
        try{
            return ResponseEntity.ok(
                    productService.add(product)
            );
        }catch (IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }catch (Exception e){
            return  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/id/{id}")
    public ResponseEntity<Product> update(
        @PathVariable Long id,
        @RequestBody Product product
    ) {
        try{
            return ResponseEntity.ok(productService.update(id, product));
        }catch(Exception e){
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/id/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        try {
            return ResponseEntity.noContent().build();
        }catch(Exception e){
            return ResponseEntity.notFound().build();
        }
    }

    // --- Custom Filters ---

    @GetMapping("/filter/collectible/{collectible}")
    public ResponseEntity<List<Product>> getByCollectible(
        @PathVariable Boolean collectible
    ) {
        try{
            return ResponseEntity.ok(productService.findByCollectible(collectible));
        }catch(Exception e){
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/filter/coefficient/{coefficient}")
    public ResponseEntity<List<Product>> getByCoefficient(
        @PathVariable Integer coefficient
    ) {
        try {
            return ResponseEntity.ok(productService.findByCoefficient(coefficient));
        }catch(Exception e){
            return ResponseEntity.notFound().build();
        }
    }
}
