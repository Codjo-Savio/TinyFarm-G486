package com.api.tinyfarm.repository;

import com.api.tinyfarm.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> { // Integer car u_id est un INTEGER

    // Chercher un user par son nom
    Optional<User> findByNom(String nom);

    // Chercher tous les users d'un niveau donné
    // Utile pour le classement du PDF (section 2.2)
    java.util.List<User> findByLevel(Integer level);
}