package com.api.tinyfarm.repository;

import com.api.tinyfarm.model.Chicken;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChickenRepository extends JpaRepository<Chicken, Long> {

    Optional<Chicken> findById(Long id);
}