package com.api.tinyfarm.repository;

import com.api.tinyfarm.model.Rabbit;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RabbitRepository extends JpaRepository<Rabbit, Long> {
    Optional<Rabbit> findById(Long id);
    List<Rabbit> findByName(String name);
    void deleteAll();
}
