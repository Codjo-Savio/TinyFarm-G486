package com.api.tinyfarm.repository;

import com.api.tinyfarm.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByName(String name);
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
    Optional<List<User>> deleteByHibernationTrueAndHibernationDateBefore(LocalDateTime date);
}
