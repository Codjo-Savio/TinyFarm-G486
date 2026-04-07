package com.api.tinyfarm.service;

import com.api.tinyfarm.model.Cooperative;
import com.api.tinyfarm.model.Product;
import com.api.tinyfarm.repository.CooperativeRepository;
import com.api.tinyfarm.repository.ProductRepository;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
public class CooperativeService {

    private final CooperativeRepository cooperativeRepository;
    private final ProductRepository productRepository;

    public CooperativeService(CooperativeRepository cooperativeRepository, ProductRepository productRepository) {
        this.cooperativeRepository = cooperativeRepository;
        this.productRepository = productRepository;
    }

    public List<Product> getAvailableProducts() {
        if (cooperativeService.isOpen)
        List<Cooperative> openCooperatives = cooperativeRepository.findByIsOpen(true);
        List<Long> productIds = openCooperatives.stream()
            .map(Cooperative::getProductId)
            .collect(Collectors.toList());
        return productRepository.findAllById(productIds);
    }
}
