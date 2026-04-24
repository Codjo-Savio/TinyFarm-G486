package com.api.tinyfarm.repository;

import com.api.tinyfarm.model.Animal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnimalRepository extends JpaRepository<Animal, Long>{
    // Animal
    Optional<Animal> findById(Long id);
    Optional<Animal> findByUserId(Long userId);
    void deleteAll();
    void deleteById(Long id);
}
