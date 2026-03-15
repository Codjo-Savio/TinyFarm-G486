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

    // Récupérer tous les users
    public List<User> findAll() {
        return userRepository.findAll();
    }

    // Récupérer un user par son id
    public User findById(Integer uId) {
        return userRepository.findById(uId)
            .orElseThrow(() -> new RuntimeException("User introuvable : " + uId));
    }

    // Créer un user — le PDF dit : démarre avec 1500 écus et niveau 1
    public User create(User user) {
        user.setEcus(1500); // Emprunt de départ obligatoire
        user.setLevel(1);   // Toujours niveau 1 au départ
        return userRepository.save(user);
    }

    // Mettre à jour un user
    public User update(Integer uId, User userModifie) {
        User existing = findById(uId);
        existing.setNom(userModifie.getNom());
        existing.setSexe(userModifie.getSexe());
        existing.setEcus(userModifie.getEcus());
        existing.setLevel(userModifie.getLevel());
        return userRepository.save(existing);
    }

    // Supprimer un user
    public void delete(Integer uId) {
        userRepository.deleteById(uId);
    }

    // Ajouter des écus (vente d'oeufs, lapins, lait...)
    // PDF : oeuf = 8 écus, lapin = 25 écus, lait = 2 écus/litre
    public User ajouterEcus(Integer uId, Integer montant) {
        User user = findById(uId);
        user.setEcus(user.getEcus() + montant);
        return userRepository.save(user);
    }

    // Retirer des écus (nourrir animaux, achats coopérative...)
    public User retirerEcus(Integer uId, Integer montant) {
        User user = findById(uId);
        if (user.getEcus() < montant) {
            throw new RuntimeException("Pas assez d'écus !");
        }
        user.setEcus(user.getEcus() - montant);
        return userRepository.save(user);
    }

    // Vérifier si le user peut encore acheter aujourd'hui
    // PDF : niveau 1 = max 12 achats par jour
    public boolean peutAcheter(Integer uId, Integer nbAchatsAujourdhui) {
        User user = findById(uId);
        int maxAchats = user.getLevel() * 12; // niveau 1 = 12 achats
        return nbAchatsAujourdhui < maxAchats;
    }
}