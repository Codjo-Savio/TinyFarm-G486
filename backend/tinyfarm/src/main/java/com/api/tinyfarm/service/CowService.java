package com.api.tinyfarm.service;

import com.api.tinyfarm.model.Cow;
import com.api.tinyfarm.model.User;
import com.api.tinyfarm.repository.CowRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CowService {

    @Autowired
    private CowRepository cowRepository;

    @Autowired
    private UserService userService;

    public List<Cow> findAll() {
        return cowRepository.findAll();
    }

    public Cow findById(Long id) {
        return cowRepository
            .findById(id)
            .orElseThrow(() ->
                new RuntimeException("Vache introuvable : " + id)
            );
    }

    public Cow getByName(String name) {
        return cowRepository
            .findByName(name)
            .orElseThrow(() ->
                new RuntimeException("Vache introuvable : " + name)
            );
    }

    public Cow create(Cow cow) {
        Authentication authentication =
            SecurityContextHolder.getContext().getAuthentication();
        if (
            authentication != null &&
            authentication.getPrincipal() instanceof User currentUser
        ) {
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

    public Cow feedCow(Long cowId, Long userId) {
        Cow cow = findById(cowId);
        User user = userService.findById(userId);

        if (user.getEcus() >= 5) {
            user.setEcus(user.getEcus() - 5);
            userService.update(user.getId(), user);

            cow.setFedToday(true);
            return cowRepository.save(cow);
        } else {
            throw new RuntimeException(
                "Pas assez d'écus pour nourrir la vache !"
            );
        }
    }

    public Cow waterCow(Long cowId, Long userId) {
        Cow cow = findById(cowId);
        User user = userService.findById(userId);

        if (user.getEcus() >= 2) {
            user.setEcus(user.getEcus() - 2);
            userService.update(user.getId(), user);

            cow.setWateredToday(true);
            return cowRepository.save(cow);
        } else {
            throw new RuntimeException(
                "Pas assez d'écus pour abreuver la vache !"
            );
        }
    }

    public Cow cleanCow(Long cowId, Long userId) {
        Cow cow = findById(cowId);
        User user = userService.findById(userId);

        if (user.getEcus() >= 3) {
            user.setEcus(user.getEcus() - 3);
            userService.update(user.getId(), user);

            cow.setClean(true);
            return cowRepository.save(cow);
        } else {
            throw new RuntimeException(
                "Pas assez d'écus pour nettoyer la vache !"
            );
        }
    }

    public Cow healCow(Long cowId, Long userId) {
        Cow cow = findById(cowId);
        User user = userService.findById(userId);

        if (user.getEcus() >= 6) {
            user.setEcus(user.getEcus() - 6);
            userService.update(user.getId(), user);

            cow.setHealthy(true);
            return cowRepository.save(cow);
        } else {
            throw new RuntimeException(
                "Pas assez d'écus pour soigner la vache !"
            );
        }
    }

    public void processEndOfDay(Long userId) {
        User user = userService.findById(userId);
        if (user.getHibernation() == null || !user.getHibernation()) {
            List<Cow> userCows = cowRepository
                .findAll()
                .stream()
                .filter(c -> userId.equals(c.getUserId()))
                .toList();

            for (Cow cow : userCows) {
                if (!cow.getHealthy()) {
                    // Logic for death after 4 sick days could be added here if model tracks it.
                }

                if (cow.getFedToday() != null && cow.getFedToday()) {
                    float weightGain = 5f; // Herbe
                    if (
                        cow.getWateredToday() != null && cow.getWateredToday()
                    ) {
                        weightGain += 1f; // Eau
                    }
                    cow.setWeight(cow.getWeight() + weightGain);
                }

                if (cow.getWeight() > 750f) {
                    cow.setWeight(750f);
                }

                cow.setAge((cow.getAge() == null ? 0 : cow.getAge()) + 1);

                // Production de lait
                if (
                    cow.getWeight() >= 80f &&
                    cow.getAge() >= 10 &&
                    cow.getHealthy() &&
                    cow.getClean() &&
                    cow.getFedToday()
                ) {
                    int milkProduced = (cow.getMilking() != null &&
                        cow.getMilking())
                        ? 16
                        : 8;
                    user.setEcus(user.getEcus() + (milkProduced * 2));
                    userService.update(user.getId(), user);
                }

                cow.setFedToday(false);
                cow.setWateredToday(false);
                cow.setClean(false);

                cowRepository.save(cow);
            }
        }
    }
}
