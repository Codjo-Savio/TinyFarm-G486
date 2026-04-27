package com.api.tinyfarm.repository;

import com.api.tinyfarm.model.Animal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnimalRepository extends JpaRepository<Animal, Long>{
    void deleteAll();
}
