package com.api.tinyfarm.repository;

import com.api.tinyfarm.model.Rabbit;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RabbitRepository extends JpaRepository<Rabbit, Long> {
    List<Rabbit> findAll();
    Optional<Rabbit> findByAID(Long aid);
    List<Rabbit> findByName(String name);
    List<Rabbit> findByRabbitEnumType(Rabbit.RabbitEnumType rabbitEnumType);
    void deleteByAID(Long aid);
    void deleteByName(String name);
    void deleteByRabbitEnumType(Rabbit.RabbitEnumType rabbitEnumType);
    void deleteAll();
}
