package com.api.tinyfarm.service;

import com.api.tinyfarm.model.User;
import com.api.tinyfarm.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(Integer id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Utilisateur introuvale : " + id));
    }

    public User create(User user) {
        user.setEcus(1500);
        user.setLevel(1);
        return userRepository.save(user);
    }

    public User update(Integer id, User modificatedUser) {
        User existing = findById(id);
        existing.setName(modificatedUser.getName());
        existing.setGender(modificatedUser.getGender());
        existing.setEcus(modificatedUser.getEcus());
        existing.setLevel(modificatedUser.getLevel());
        return userRepository.save(existing);
    }

    public void delete(Integer id) {
        userRepository.deleteById(id);
    }

    public User addEcus(Integer id, Integer amount) {
        User user = findById(id);
        user.setEcus(user.getEcus() + amount);
        return userRepository.save(user);
    }

    public User withdrawEcus(Integer id, Integer amount) {
        User user = findById(id);
        if (user.getEcus() < amount) {
            throw new RuntimeException("Pas assez d'écus !");
        }
        user.setEcus(user.getEcus() - amount);
        return userRepository.save(user);
    }

    public boolean canBuy(Integer id, Integer purchaseNumberForToday) {
        User user = findById(id);
        int maxPurchase = user.getLevel() * 12; // niveau 1 = 12 achats  par jour
        return purchaseNumberForToday < maxPurchase;
    }
}