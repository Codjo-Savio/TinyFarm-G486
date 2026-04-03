package com.api.tinyfarm.service;

import com.api.tinyfarm.model.Animal;
import com.api.tinyfarm.model.Chicken;
import com.api.tinyfarm.model.User;
import com.api.tinyfarm.repository.ChickenRepository;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class ChickenService {

    private final ChickenRepository chickenRepository;
    private final UserService userService;

    public ChickenService(
        ChickenRepository chickenRepository,
        UserService userService
    ) {
        this.chickenRepository = chickenRepository;
        this.userService = userService;
    }

    public List<Chicken> findAll() {
        return chickenRepository.findAll();
    }

    public Chicken findById(Long id) {
        return chickenRepository
            .findById(id)
            .orElseThrow(() ->
                new RuntimeException("Poulet introuvable : " + id)
            );
    }

    public Chicken getByName(String name) {
        return chickenRepository
            .findByName(name)
            .orElseThrow(() ->
                new RuntimeException("Poulet introuvable : " + name)
            );
    }

    public Chicken create(Chicken chicken) {
        if (chicken.getWeight() == null) {
            chicken.setWeight(0.05f); // Poids de naissance
        }
        if (chicken.getAge() == null) {
            chicken.setAge(0);
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User currentUser) {
            chicken.setUserId(currentUser.getId());
        }
        return chickenRepository.save(chicken);
    }

    public Chicken update(Long id, Chicken modificatedChicken) {
        Chicken existing = findById(id);
        existing.setChickenType(modificatedChicken.getChickenType());
        existing.setName(modificatedChicken.getName());
        existing.setFastingDays(modificatedChicken.getFastingDays());
        existing.setSickDays(modificatedChicken.getSickDays());

        // Animal properties
        existing.setClean(modificatedChicken.getClean());
        existing.setHealthy(modificatedChicken.getHealthy());
        existing.setAge(modificatedChicken.getAge());
        existing.setWeight(modificatedChicken.getWeight());
        existing.setGender(modificatedChicken.getGender());

        return chickenRepository.save(existing);
    }

    public void delete(Long id) {
        chickenRepository.deleteById(id);
    }

    public void deleteByName(String name) {
        chickenRepository.deleteByName(name);
    }

    public void deleteAll() {
        chickenRepository.deleteAll();
    }

    // --- Actions Journalières ---

    public Chicken feedChicken(Long chickenId, Long userId) {
        Chicken chicken = findById(chickenId);
        User user = userService.findById(userId);

        if (user.getEcus() >= 3) {
            user.setEcus(user.getEcus() - 3);
            userService.update(user.getId(), user);

            chicken.setFedToday(true);
            return chickenRepository.save(chicken);
        } else {
            throw new RuntimeException(
                "Pas assez d'écus pour nourrir la volaille !"
            );
        }
    }

    public Chicken waterChicken(Long chickenId, Long userId) {
        Chicken chicken = findById(chickenId);
        User user = userService.findById(userId);

        if (user.getEcus() >= 1) {
            user.setEcus(user.getEcus() - 1);
            userService.update(user.getId(), user);

            chicken.setWateredToday(true);
            return chickenRepository.save(chicken);
        } else {
            throw new RuntimeException(
                "Pas assez d'écus pour abreuver la volaille !"
            );
        }
    }

    public Chicken cleanChicken(Long chickenId, Long userId) {
        Chicken chicken = findById(chickenId);
        User user = userService.findById(userId);

        if (user.getEcus() >= 3) {
            user.setEcus(user.getEcus() - 3);
            userService.update(user.getId(), user);

            chicken.setClean(true);
            return chickenRepository.save(chicken);
        } else {
            throw new RuntimeException(
                "Pas assez d'écus pour nettoyer la volaille !"
            );
        }
    }

    public Chicken healChicken(Long chickenId, Long userId) {
        Chicken chicken = findById(chickenId);
        User user = userService.findById(userId);

        if (user.getEcus() >= 6) {
            user.setEcus(user.getEcus() - 6);
            userService.update(user.getId(), user);

            chicken.setHealthy(true);
            chicken.setSickDays(0);
            return chickenRepository.save(chicken);
        } else {
            throw new RuntimeException(
                "Pas assez d'écus pour soigner la volaille !"
            );
        }
    }

    // --- Fin de journée ---

    public void processEndOfDay(Long userId) {
        List<Chicken> userChickens = chickenRepository.findByUserId(userId);

        long activeRoosters = 0;
        long activeHens = 0;

        for (Chicken chicken : userChickens) {
            // 1. Santé
            if (handleHealth(chicken) != 0){
                continue;
            }

            // 2. Faim, Soif et Poids
            if (handleFood(chicken) != 0){
                continue;
            }

            // Poids max
            if (chicken.getWeight() > 3.5f) {
                chicken.setWeight(3.5f);
            }

            // Mort par maigreur
            if (chicken.getWeight() <= 0f) {
                chickenRepository.delete(chicken);
                continue;
            }

            // 3. Âge et Évolution
            chicken.setAge(
                (chicken.getAge() == null ? 0 : chicken.getAge()) + 1
            );

            // 4. Changement de type de poulet
            handleType(chicken);
            
            // 5. Saleté
            if (!chicken.getClean()){
                if (chicken.getChickenType() == Chicken.ChickenType.L) {
                    chicken.setChickenType(Chicken.ChickenType.H);
                } else if (chicken.getChickenType() == Chicken.ChickenType.B) {
                    chicken.setChickenType(Chicken.ChickenType.R);
                }
            }

            // Comptage pour la ponte (doit être adulte, propre, sain, et nourri)
            if (chicken.getChickenType() == Chicken.ChickenType.B) {
                activeRoosters++;
            } else if (chicken.getChickenType() == Chicken.ChickenType.L) {
                activeHens++;
            }

            // Réinitialisation journalière
            chicken.setFedToday(false);
            chicken.setWateredToday(false);
            chicken.setClean(false); // Le poulailler devient sale tous les jours

            chickenRepository.save(chicken);
        }

        // 4. Ponte et Vente automatique des œufs (vendus le jour même)
        handleEggs(userId, activeRoosters, activeHens);
    }

    // renvoi un code 1 si le poulet meurt
    private int handleHealth(Chicken chicken){

        int out = 0;

        if (chicken.getHealthy() != null && !chicken.getHealthy()) {
            chicken.setSickDays(
                (chicken.getSickDays() == null
                        ? 0
                        : chicken.getSickDays()) + 1
                );

            // si il est malade il ne peu pas se reproduire
            if (chicken.getChickenType() == Chicken.ChickenType.L) {
                chicken.setChickenType(Chicken.ChickenType.H);
            } else if (chicken.getChickenType() == Chicken.ChickenType.B) {
                chicken.setChickenType(Chicken.ChickenType.R);
            }

            if (chicken.getSickDays() >= 4) {
                chickenRepository.delete(chicken);
                return 1; // le poulet meurt
            }
        } else {
            chicken.setSickDays(0);
        }

        return out;
    }

    private int handleFood(Chicken chicken){

        int out = 0;

        if (chicken.getFedToday() != null && !chicken.getFedToday()) {
            chicken.setFastingDays(
                (chicken.getFastingDays() == null
                        ? 0
                        : chicken.getFastingDays()) + 1
                );

            // si il a faim il ne peu pas se reproduire
            if (chicken.getChickenType() == Chicken.ChickenType.L) {
                chicken.setChickenType(Chicken.ChickenType.H);
            } else if (chicken.getChickenType() == Chicken.ChickenType.B) {
                chicken.setChickenType(Chicken.ChickenType.R);
            }

            float weightLoss = 0f;
            if (chicken.getFastingDays() == 1) weightLoss = 0.2f;
            else if (chicken.getFastingDays() == 2) weightLoss = 0.5f;
            else if (chicken.getFastingDays() == 3) weightLoss = 1.0f;
            else if (chicken.getFastingDays() >= 4) {
                chickenRepository.delete(chicken);
                return 1; // le poulet meurt
            }
            chicken.setWeight(chicken.getWeight() - weightLoss);
        } else {
            chicken.setFastingDays(0);
            float weightGain = 0.5f; // Grains
            if (
                chicken.getWateredToday() != null &&
                chicken.getWateredToday()
            ) {
                weightGain += 0.15f; // Eau (seulement si nourri selon la règle "toute seule elle ne fait pas grossir")
            }
            chicken.setWeight(chicken.getWeight() + weightGain);
        }

        return out;
    }

    private void handleType(Chicken chicken){

        // Retour en élevage si perte de poids
        if (
            chicken.getChickenType() == Chicken.ChickenType.L &&
            chicken.getWeight() < 2.5f
        ) {
            chicken.setChickenType(Chicken.ChickenType.H);
        }

        if (
            chicken.getChickenType() == Chicken.ChickenType.B &&
            chicken.getWeight() < 2.5f
        ) {
            chicken.setChickenType(Chicken.ChickenType.R);
        }

        // Passage adulte
        if (
            chicken.getChickenType() == Chicken.ChickenType.C &&
            chicken.getAge() == 4
        ) {
            if (Math.random() > 0.5) {
                chicken.setChickenType(Chicken.ChickenType.H);
            } else {
               chicken.setChickenType(Chicken.ChickenType.R);
            }
        }

        // Passage reproducteur
        if (
            chicken.getChickenType() == Chicken.ChickenType.H &&
            chicken.getAge() >= 5 &&
            chicken.getWeight() >= 2.5f
        ) {
            chicken.setChickenType(Chicken.ChickenType.L);
        }
        if (
            chicken.getChickenType() == Chicken.ChickenType.R &&
            chicken.getAge() >= 5 &&
            chicken.getWeight() >= 2.5f
        ) {
            chicken.setChickenType(Chicken.ChickenType.B);
        }
    }

    private void handleEggs(Long userId, long activeRoosters, long activeHens) {
        // Chaque coq peut féconder jusqu'à 5 poules
        long matedHens = Math.min(activeHens, activeRoosters * 5);
        int totalEggs = 0;

        for (int i = 0; i < matedHens; i++) {
            double rand = Math.random();
            if (rand < 0.33) {
                totalEggs += 0;
            } else if (rand < 0.66) {
                totalEggs += 1;
            } else {
                totalEggs += 2;
            }
        }

        // Vente des œufs à la coopérative (8 écus/oeuf)
        if (totalEggs > 0) {
            User user = userService.findById(userId);
            user.setEcus(user.getEcus() + (totalEggs * 8));
            userService.update(user.getId(), user);
        }
    }
}
