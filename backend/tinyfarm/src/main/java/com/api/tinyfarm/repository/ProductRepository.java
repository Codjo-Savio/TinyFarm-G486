package com.api.tinyfarm.repository;

import com.api.tinyfarm.model.Product;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // JpaRepository provides findById(Long id) by default, no need to redefine it.

    List<Product> findByCollectible(Boolean collectible);

    List<Product> findByCoefficient(Integer coefficient);
}
