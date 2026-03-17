package com.api.tinyfarm.repository;

import com.api.tinyfarm.model.Animal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface AnimalRepository {
    // Animal

    Optional<List<Animal>> findByAll();
    Optional<Animal> findByName(String name);
    Optional<Animal> findByUID(int UID);
    Optional<List<Animal>> findByClean(Boolean clean);
    Optional<List<Animal>> findByHealthy(Boolean healthy);
    Optional<List<Animal>> findByAge(int age);
    Optional<List<Animal>> findByWeight(float weight);
    Optional<List<Animal>> findByGender(Boolean gender);
}
