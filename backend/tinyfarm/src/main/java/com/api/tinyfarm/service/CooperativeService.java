package com.api.tinyfarm.service;

import java.util.HashMap;

import com.api.tinyfarm.model.Cooperative;
import com.api.tinyfarm.model.Product;
import com.api.tinyfarm.repository.CooperativeRepository;
import com.api.tinyfarm.repository.ProductRepository;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.api.tinyfarm.model.Market;
import com.api.tinyfarm.model.User;

@Service
public class CooperativeService {

    @Autowired
    private CooperativeRepository cooperativeRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductService productService;
    @Autowired
    private UserService userService;

    
    public HashMap<Product, Float> getAvailableProducts() {
        HashMap<Product, Float> productPrices = new HashMap<>();

        List<Cooperative> cooperatives = cooperativeRepository.findAll();
        for (Cooperative coop : cooperatives) {
            Long productId = coop.getProductId();
            Long userId = coop.getUserId();
            User user = userService.findById(userId);
            Product product = productService.findById(productId);
            Market market = 
            if (product != null) {
                productPrices.put(product, productPrices.getOrDefault(product, 0F) + product.getPrice());
            }
        }

        return productPrices;
    }
}
