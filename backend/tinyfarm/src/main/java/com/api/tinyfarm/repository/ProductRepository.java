package com.api.tinyfarm.repository;

import com.api.tinyfarm.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<List<Product>> findAll();
    Optional<Product> findById(Long id);
}
