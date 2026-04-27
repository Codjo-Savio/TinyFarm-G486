package com.api.tinyfarm.repository;

import com.api.tinyfarm.model.Chicken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChickenRepository extends JpaRepository<Chicken, Long> {
    Optional<Chicken> findByName(String name);
    void deleteByName(String name);

    java.util.List<Chicken> findByUserId(Long userId);
}
