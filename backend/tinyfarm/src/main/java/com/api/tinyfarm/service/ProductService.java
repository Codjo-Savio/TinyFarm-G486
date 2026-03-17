package com.api.tinyfarm.service;

import com.api.tinyfarm.model.Product;
import com.api.tinyfarm.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }


    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public void deleteAllUsers(){
       productRepository.deleteAll();
    }

    public Product findById(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Produit introuvale : " + id));
    }

    public Product add(Product product) {
        return productRepository.save(product);
    }
    public Product update(Long id, Product modificatedProduct) {
        Product existing = findById(id);
        existing.setDescription(modificatedProduct.getDescription());
        existing.setPrice(modificatedProduct.getPrice());
        existing.setCollectible(modificatedProduct.getCollectible());
        existing.setCoefficient(modificatedProduct.getCoefficient());
        return productRepository.save(existing);
    }

    public void delete(Long id) {
        productRepository.deleteById(id);
    }

    public void deleteAllProducts(){
        productRepository.deleteAll();
    }
}
