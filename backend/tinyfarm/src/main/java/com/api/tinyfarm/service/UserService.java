package com.api.tinyfarm.service;

import com.api.tinyfarm.model.User;
import com.api.tinyfarm.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public void deleteAllUsers() {
        userRepository.deleteAll();
    }

    public User findOrCreateOAuthUser(
            String email,
            String name,
            User.Gender gender) {
        return userRepository
                .findByEmail(email)
                .map(existing -> {
                    existing.setName(name);
                    existing.setGender(gender);

                    // handle hibernation
                    if(Boolean.TRUE.equals(existing.getHibernation())){
                        existing.setHibernation(false);
                        existing.setHibernationDate(null);
                    }

                    return userRepository.save(existing);
                })
                .orElseGet(() -> userRepository.save(User.builder()
                        .email(email)
                        .name(name)
                        .gender(gender)
                        .build()));
    }

    public User findById(Long id) {
        return userRepository
                .findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable : " + id));
    }

    public User findByEmail(String email) {
        return userRepository
                .findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable : " + email));
    }

    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public User create(User user) {
        return userRepository.save(user);
    }

    public User update(Long id, User modificatedUser) {
        User existing = findById(id);
        existing.setName(modificatedUser.getName());
        existing.setGender(modificatedUser.getGender());
        existing.setEcus(modificatedUser.getEcus());
        existing.setHibernation(modificatedUser.getHibernation());
        existing.setLevel(modificatedUser.getLevel());
        return userRepository.save(existing);
    }

    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    public User addEcus(Long id, Float amount) {
        User user = findById(id);
        user.setEcus(user.getEcus() + amount);
        return userRepository.save(user);
    }

    public User withdrawEcus(Long id, Float amount) {
        User user = findById(id);
        if (user.getEcus() < amount) {
            throw new RuntimeException("Pas assez d'écus !");
        }
        user.setEcus(user.getEcus() - amount);
        return userRepository.save(user);
    }

    public void hibernate(Long id) {
        User user = findById(id);
        user.setHibernation(true);
        user.setHibernationDate(LocalDateTime.now());
        userRepository.save(user);
    }

    // deleting users that have hibernated for more than 50 day every day at midnight
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void deleteExpiredHibernations() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(50);
        userRepository.deleteByHibernationTrueAndHibernationDateBefore(cutoff);
    }

    // reset remaining purchases every day at midnight
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void resetRemainingPurchases() {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            user.setRemainingPurchases(12);
        }
        userRepository.saveAll(users);
    }

}
