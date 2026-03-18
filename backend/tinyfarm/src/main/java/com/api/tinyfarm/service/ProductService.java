package com.api.tinyfarm.service;

import com.api.tinyfarm.model.Product;
import com.api.tinyfarm.repository.ProductRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Product findById(Long id) {
        return productRepository
            .findById(id)
            .orElseThrow(() ->
                new RuntimeException("Produit introuvable : " + id)
            );
    }

    public Product add(Product product) {
        return productRepository.save(product);
    }

    public Product update(Long id, Product modifiedProduct) {
        Product existing = findById(id);

        // Update fields
        existing.setDescription(modifiedProduct.getDescription());
        existing.setPrice(modifiedProduct.getPrice());
        existing.setCollectible(modifiedProduct.getCollectible());
        existing.setCoefficient(modifiedProduct.getCoefficient());

        return productRepository.save(existing);
    }

    public void delete(Long id) {
        productRepository.deleteById(id);
    }

    public void deleteAllProducts() {
        productRepository.deleteAll();
    }

    // --- Custom Queries ---

    public List<Product> findByCollectible(Boolean collectible) {
        return productRepository.findByCollectible(collectible);
    }

    public List<Product> findByCoefficient(Integer coefficient) {
        return productRepository.findByCoefficient(coefficient);
    }
}
