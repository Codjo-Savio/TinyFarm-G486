package com.api.tinyfarm.repository;

import com.api.tinyfarm.model.Animal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnimalRepository extends JpaRepository<Animal, Long> {
    Optional<Animal> findByName(String name);

    Optional<Animal> findByUID(int uid);

    // In Spring Data JPA, returning a List is preferred over Optional<List<T>>
    // because it will return an empty list if no results are found.
    List<Animal> findByClean(Boolean clean);

    List<Animal> findByHealthy(Boolean healthy);

    List<Animal> findByAge(int age);

    List<Animal> findByWeight(float weight);

    List<Animal> findByGender(Boolean gender);
}
