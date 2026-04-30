package com.api.tinyfarm.service;

import com.api.tinyfarm.model.Cow;
import com.api.tinyfarm.model.Rabbit;
import com.api.tinyfarm.model.User;
import com.api.tinyfarm.repository.CowRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CowService {

    @Autowired
    private CowRepository cowRepository;

    @Autowired
    private UserService userService;
    
    public List<Cow> findAll() {
        return cowRepository.findAll();
    }

    public List<Cow> findByConnectedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User currentUser)) {
            throw new RuntimeException("Utilisateur non authentifié");
        }
        return cowRepository.findByUserId(currentUser.getId());
    }

    public Cow findById(Long id) {
        return cowRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vache introuvable : " + id));
    }

    public Cow getByName(String name) {
        return cowRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Vache introuvable : " + name));
    }

    public Cow create(Cow cow) {
        if (cow == null) {
            throw new IllegalArgumentException("Vache manquante");
        }
        if (cow.getName() == null || cow.getName().isBlank()) {
            throw new IllegalArgumentException("Nom de la vache manquant");
        }
        if (cow.getId() != null && cowRepository.existsById(cow.getId())) {
            throw new IllegalArgumentException("Vache déjà existante : " + cow.getId());
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User currentUser) {
            cow.setUserId(currentUser.getId());
        }
        return cowRepository.save(cow);
    }

    public Cow update(Long id, Cow modifiedCow) {
        Cow existing = findById(id);
        existing.setCowType(modifiedCow.getCowType());
        existing.setName(modifiedCow.getName());
        existing.setMilking(modifiedCow.getMilking());
        return cowRepository.save(existing);
    }

    public void delete(Long id) {
        cowRepository.deleteById(id);
    }

    public void deleteByName(String name) {
        cowRepository.deleteByName(name);
    }

    public void deleteAll() {
        cowRepository.deleteAll();
    }

    // --- Daily Actions ---

    /**
     * Feeds a cow with hay.
     */
    public Cow hayCow(Long cowId, Long userId) {
        Cow cow = findById(cowId);
        User user = userService.findById(userId);

        if (user.getEcus() >= -1495) {
            user.setEcus(user.getEcus() - 5);
            userService.update(userId, user);

            cow.setHayToday(true);
            cow.setFedToday(true);
            return cowRepository.save(cow);
        } else {
            throw new RuntimeException(
                    "Pas assez d'écus pour nourrir la vahe avec de la paille !");
        }
    }

    /**
     * Daily fallback feeding with grass for all cows.
     */
    @Scheduled(cron = "12 0 0 * * *")
    @Transactional
    public void grassCow() {
        List<Cow> cows = cowRepository.findAll();
        for(Cow cow : cows){
            cow.setFedToday(true);
            cowRepository.save(cow);
        }
    }

    /**
     * Waters a cow.
     */
    public Cow waterCow(Long cowId, Long userId) {
        Cow cow = findById(cowId);
        User user = userService.findById(userId);

        if (user.getEcus() >= -1498) {
            user.setEcus(user.getEcus() - 2);
            userService.update(userId, user);

            cow.setWateredToday(true);
            return cowRepository.save(cow);
        } else {
            throw new RuntimeException(
                    "Pas assez d'écus pour abreuver la vache !");
        }
    }

    /**
     * Cleans a cow.
     */
    public Cow cleanCow(Long cowId, Long userId) {
        Cow cow = findById(cowId);
        User user = userService.findById(userId);

        if (user.getEcus() >= -1497) {
            user.setEcus(user.getEcus() - 3);
            userService.update(userId, user);

            cow.setClean(true);
            return cowRepository.save(cow);
        } else {
            throw new RuntimeException(
                    "Pas assez d'écus pour nettoyer la vache !");
        }
    }

    /**
     * Heals a cow.
     */
    public Cow healCow(Long cowId, Long userId) {
        Cow cow = findById(cowId);
        User user = userService.findById(userId);

        if (user.getEcus() >= -1494) {
            user.setEcus(user.getEcus() - 6);
            userService.update(userId, user);

            cow.setHealthy(true);
            return cowRepository.save(cow);
        } else {
            throw new RuntimeException(
                    "Pas assez d'écus pour soigner la vache !");
        }
    }

    /**
     * Updates milk quantity based on milking eligibility.
     */
    public void milking(Long cowId) {
        Cow cow = findById(cowId);

        // Milk production only happens while milking is allowed.
        if (cow.getMilking()) {
            // First production tick starts from 8 liters, then +4 each day.
            if (cow.getMilk() == 0) {
                cow.setMilk(8);
            } else {
                cow.setMilk(cow.getMilk() + 4);
            }
        }

        if (cow.getMilk() == 16) {
            cow.setMilking(false);
        }

        cowRepository.save(cow);
    }

    /**
     * Adjusts cow weight from today's feeding and watering actions.
     */
    public Cow handleWeight(Long cowId) {
        Cow cow = findById(cowId);

        if (cow.getWateredToday()) {
            if (cow.getHayToday()) {
                // hay + grass + water
                cow.setWeight(cow.getWeight() + 9);
            } else {
                // grass + water
                cow.setWeight(cow.getWeight() + 6);
            }

        } else {
            if (cow.getHayToday()) {
                // hay + grass
                cow.setWeight(cow.getWeight() + 8);
            } else {
                // grass only
                cow.setWeight(cow.getWeight() + 5);
            }

        }

        // Water alone has no direct weight effect.

        // Maximum weight cap: 750 kg.
        if (cow.getWeight() > 750.0f) {
            cow.setWeight(750.0f);
        }

        return cowRepository.save(cow);
    }

    /**
     * Updates health progression for one cow.
     */
    private Cow handleHealth(Long cowId) {
        // A non-healthy cow accumulates sick days; healthy cows reset sick days and can produce milk.
        Cow cow = findById(cowId);

        if (cow.getHealthy() != null && !cow.getHealthy()) {
            cow.setSickDays(cow.getSickDays() + 1);
        } else {
            cow.setSickDays(0);
            cow.setMilking(true);
        }

        return cowRepository.save(cow);
    }

    /**
     * Applies end-of-day lifecycle for all cows of a user.
     */
    public void processEndOfDay(Long userId) {
        // End-of-day applies health/weight rules, daily resets, and random illness chance.
        List<Cow> userCows = cowRepository.findByUserId(userId);
        for (Cow cow : userCows) {
            Cow processedCow = handleHealth(cow.getId());
            if (shouldDeleteCowAfterHealth(processedCow)) {
                cowRepository.delete(processedCow);
                continue;
            }
            processedCow = handleWeight(processedCow.getId());
            applyDailyCowReset(processedCow);
            applyRandomIllness(processedCow);
            cowRepository.save(processedCow);
        }
    }

    private boolean shouldDeleteCowAfterHealth(Cow cow) {
        // Cows die when sickness reaches 4 consecutive days.
        return cow.getSickDays() == 4;
    }

    private void applyDailyCowReset(Cow cow) {
        // End-of-day resets clear all action flags and milk eligibility.
        cow.setFedToday(false);
        cow.setHayToday(false);
        cow.setWateredToday(false);
        cow.setClean(false);
        cow.setMilking(false);
    }

    private void applyRandomIllness(Cow cow) {
        // Base disease chance at end of day: 20%.
        if (Math.random() < 0.2) {
            cow.setHealthy(false);
            cow.setMilking(false);
        }
    }

}
