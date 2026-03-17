package com.api.tinyfarm.repository;

import com.api.tinyfarm.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByName(String name);
    Optional<User> findById(Long id);
    Optional<List<User>> findByLevel(Integer level);
    Optional<List<User>> findByHibernation(Boolean hibernation);
    Optional<List<User>> findByGender(User.Gender gender);
    Optional<List<User>> findByEcus(Integer ecus);
}
