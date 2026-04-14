package com.api.tinyfarm.service;

import java.util.HashMap;
import java.util.Map;

import com.api.tinyfarm.model.Cooperative;
import com.api.tinyfarm.model.Product;
import com.api.tinyfarm.repository.CooperativeRepository;
import com.api.tinyfarm.repository.ProductRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CooperativeService {

    @Autowired
    private CooperativeRepository cooperativeRepository;
    @Autowired
    private ProductRepository productRepository;

    
    public HashMap<Long, Float> getAvailableProducts() {
        HashMap<Long, Float> productPrices = new HashMap<>();
        Map<Long, Float> totalPricesByProductId = new HashMap<>();
        Map<Long, Integer> countsByProductId = new HashMap<>();

        List<Cooperative> cooperatives = cooperativeRepository.findAll();
        for (Cooperative coop : cooperatives) {
            Long productId = coop.getProductId();
            Float price = coop.getPrice();

            if (productId == null || price == null) {
                continue;
            }

            totalPricesByProductId.merge(productId, price, Float::sum);
            countsByProductId.merge(productId, 1, Integer::sum);
        }

        for (Map.Entry<Long, Float> entry : totalPricesByProductId.entrySet()) {
            Long productId = entry.getKey();
            Integer count = countsByProductId.get(productId);

            if (productId == null || count == null || count == 0) {
                continue;
            }

            Float averagePrice = entry.getValue() / count;
            productPrices.put(productId, averagePrice);
        }

        return productPrices;
    }

    public void deleteLessExpensiveWithDescription(String description) {
        List<Cooperative> cooperatives = cooperativeRepository.findAll();
        List<Cooperative> matchingCooperatives = cooperatives.stream()
                .filter(coop -> {
                    Product product = productService.findById(coop.getProductId());
                    return product != null && description.equals(product.getDescription());
                })
                .collect(Collectors.toList());

        if (!matchingCooperatives.isEmpty()) {
            Cooperative leastExpensiveCoop = matchingCooperatives.stream()
                    .min((c1, c2) -> {
                        Product p1 = productService.findById(c1.getProductId());
                        Product p2 = productService.findById(c2.getProductId());
                        return Float.compare(p1.getPrice(), p2.getPrice());
                    })
                    .orElse(null);

            if (leastExpensiveCoop != null) {
                cooperativeRepository.delete(leastExpensiveCoop);
            }
        }
    }
}
