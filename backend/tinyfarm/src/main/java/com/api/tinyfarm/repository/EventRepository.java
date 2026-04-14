package com.api.tinyfarm.repository;

import com.api.tinyfarm.model.Event;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    Optional<Event> findById(Long id);
    Optional<Event> findByUserId(Long userId);
    void deleteById(Long id);
    void deleteByUserId(Long userId);
    void deleteAll();
}
