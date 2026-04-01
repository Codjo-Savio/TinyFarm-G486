package com.api.tinyfarm.service;

import com.api.tinyfarm.model.User;
import com.api.tinyfarm.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;

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

    public User findOrCreateOAuthUser(String email, String name, User.Gender gender) {
        return userRepository.findByEmail(email)
                .map(existing -> {
                    existing.setName(name);
                    existing.setGender(gender);
                    return userRepository.save(existing);
                })
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .email(email)
                                .name(name)
                                .gender(gender)
                                .build()
                ));
    }

    public User findById(Long id) {
        return userRepository
            .findById(id)
            .orElseThrow(() ->
                new RuntimeException("Utilisateur introuvable : " + id)
            );
    }

    public User findByName(String name) {
        return userRepository
                .findByName(name)
                .orElseThrow(() ->
                        new RuntimeException("Utilisateur introuvable : " + name)
                );
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

    public User addEcus(Long id, Integer amount) {
        User user = findById(id);
        user.setEcus(user.getEcus() + amount);
        return userRepository.save(user);
    }

    public User withdrawEcus(Long id, Integer amount) {
        User user = findById(id);
        if (user.getEcus() < amount) {
            throw new RuntimeException("Pas assez d'écus !");
        }
        user.setEcus(user.getEcus() - amount);
        return userRepository.save(user);
    }

    public boolean canBuy(Long id, Integer purchaseNumberForToday) {
        User user = findById(id);
        int maxPurchase = user.getLevel() * 12; // niveau 1 = 12 achats par jour
        return purchaseNumberForToday < maxPurchase;
    }
}
