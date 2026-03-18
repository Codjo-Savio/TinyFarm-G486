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
    Optional<List<Animal>> findByClean(Boolean clean);
    Optional<List<Animal>> findByHealthy(Boolean healthy);
    Optional<List<Animal>> findByAge(int age);
    Optional<List<Animal>> findByWeight(float weight);
    Optional<List<Animal>> findByGender(Animal.AnimalGender gender);
    void deleteAll();
    void deleteById(Long id);
}
