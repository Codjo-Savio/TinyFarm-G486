package com.api.tinyfarm.service;

import com.api.tinyfarm.model.Product;
import com.api.tinyfarm.model.User;
import com.api.tinyfarm.repository.ProductRepository;
import com.api.tinyfarm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class CooperativeInitializer {

    private final UserRepository userRepository;

    @Bean
    CommandLineRunner initCooperativeUser() {
        return args -> {
            String email = "cooperative@system.local";

            if (userRepository.findByEmail(email).isEmpty()) {
                User coop = new User();
                coop.setName("Cooperative");
                coop.setEmail(email);
                coop.setEcus(999999f);
                coop.setGender(User.Gender.M);

                userRepository.save(coop);
            }
        };
    }

    @Bean
    CommandLineRunner initProducts(ProductRepository productRepository) {
        return args -> {

            List<String> defaultProducts = List.of(
                    "seau d'eau",
                    "botte de foin",
                    "botte de paille",
                    "oeuf",
                    "lait",
                    "seringue",
                    "savon",
                    "céréales",
                    "sac de nourriture"
            );

            for (String description : defaultProducts) {

                boolean exists = productRepository
                        .findByDescription(description)
                        .stream()
                        .findAny()
                        .isPresent();

                if (!exists) {
                    Product product = new Product();
                    product.setDescription(description);
                    productRepository.save(product);
                }
            }
        };
    }

    @Bean
    CommandLineRunner initCooperativeData(CooperativeService cooperativeService) {
        return args -> {
            cooperativeService.addProductsToCooperative();
        };
    }
}
