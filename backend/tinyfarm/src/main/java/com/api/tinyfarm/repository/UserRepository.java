package com.api.tinyfarm.repository;

import com.api.tinyfarm.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByName(String name);
    Optional<User> findById(Integer id);
    java.util.List<User> findByLevel(Integer level);
}