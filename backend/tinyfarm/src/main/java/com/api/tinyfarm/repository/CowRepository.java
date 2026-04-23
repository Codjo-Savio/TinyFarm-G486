package com.api.tinyfarm.repository;

import com.api.tinyfarm.model.Cow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CowRepository extends JpaRepository<Cow, Long> {

    Optional<Cow> findById(Long id);
    Optional<Cow> findByName(String name);

    void deleteByName(String name);
    void deleteAll();
    java.util.List<Cow> findByUserId(Long userId);
}
