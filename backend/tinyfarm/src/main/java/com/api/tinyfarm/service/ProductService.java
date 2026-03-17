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

    public User findById(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Produit introuvale : " + id));
    }

    public User create(Product product) {
        return userRepository.save(product);
    }
    /* TODO
    public User update(Long id, User modificatedProduct) {
        Product existing = findById(id);
        existing.setName(modificatedUser.getName());
        existing.setGender(modificatedUser.getGender());
        existing.setEcus(modificatedUser.getEcus());
        existing.setLevel(modificatedUser.getLevel());
        return productRepository.save(existing);
    } */

    public void delete(Long id) {
        productRepository.deleteById(id);
    }
}
