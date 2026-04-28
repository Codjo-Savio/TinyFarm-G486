package com.api.tinyfarm.service;

import com.api.tinyfarm.model.Animal;
import com.api.tinyfarm.model.Rabbit;
import com.api.tinyfarm.model.User;
import com.api.tinyfarm.repository.RabbitRepository;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class RabbitService {

    private final RabbitRepository rabbitRepository;
    private final UserService userService;

    public RabbitService(
        RabbitRepository rabbitRepository,
        UserService userService
    ) {
        this.rabbitRepository = rabbitRepository;
        this.userService = userService;
    }

    public List<Rabbit> findAll() {
        return rabbitRepository.findAll();
    }

    public void deleteAllRabbits() {
        rabbitRepository.deleteAll();
    }

    public Rabbit findById(Long id) {
        return rabbitRepository
            .findById(id)
            .orElseThrow(() ->
                new RuntimeException("Lapin introuvable : " + id)
            );
    }

    public Rabbit create(Rabbit rabbit) {
        if (rabbit == null) {
            throw new IllegalArgumentException("Lapin manquant");
        }
        if (rabbit.getName() == null || rabbit.getName().isBlank()) {
            throw new IllegalArgumentException("Nom du lapin manquant");
        }
        if (rabbit.getId() != null && rabbitRepository.existsById(rabbit.getId())) {
            throw new IllegalArgumentException("Lapin déjà existant : " + rabbit.getId());
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User currentUser) {
            rabbit.setUserId(currentUser.getId());
        }
        return rabbitRepository.save(rabbit);
    }

    public Rabbit update(Long id, Rabbit modifiedRabbit) {
        Rabbit existing = findById(id);

        // Update Animal inherited fields
        existing.setUserId(modifiedRabbit.getUserId());
        existing.setClean(modifiedRabbit.getClean());
        existing.setHealthy(modifiedRabbit.getHealthy());
        existing.setAge(modifiedRabbit.getAge());
        existing.setWeight(modifiedRabbit.getWeight());
        existing.setGender(modifiedRabbit.getGender());

        // Update Rabbit specific fields
        existing.setName(modifiedRabbit.getName());
        existing.setRabbitType(modifiedRabbit.getRabbitType());

        return rabbitRepository.save(existing);
    }

    public void delete(Long id) {
        rabbitRepository.deleteById(id);
    }

    // --- Filters ---

    public Rabbit findByName(String name) {
        return rabbitRepository
            .findByName(name)
            .orElseThrow(() ->
                new RuntimeException("Lapin introuvable avec le nom : " + name)
            );
    }

    public List<Rabbit> findByRabbitType(Rabbit.RabbitTypeEnum rabbitType) {
        return rabbitRepository.findByRabbitType(rabbitType);
    }

    public Rabbit feedRabbit(Long rabbitId, Long userId) {
        Rabbit rabbit = findById(rabbitId);
        User user = userService.findById(userId);

        if (user.getEcus() >= -1495) {
            user.setEcus(user.getEcus() - 5);
            userService.update(user.getId(), user);

            rabbit.setFedToday(true);
            return rabbitRepository.save(rabbit);
        } else {
            throw new RuntimeException(
                "Pas assez d'écus pour nourrir le lapin !"
            );
        }
    }

    public Rabbit waterRabbit(Long rabbitId, Long userId) {
        Rabbit rabbit = findById(rabbitId);
        User user = userService.findById(userId);

        if (user.getEcus() >= -1498) {
            user.setEcus(user.getEcus() - 2);
            userService.update(user.getId(), user);

            rabbit.setWateredToday(true);
            return rabbitRepository.save(rabbit);
        } else {
            throw new RuntimeException(
                "Pas assez d'écus pour abreuver le lapin !"
            );
        }
    }

    public Rabbit cleanRabbit(Long rabbitId, Long userId) {
        Rabbit rabbit = findById(rabbitId);
        User user = userService.findById(userId);

        if (user.getEcus() >= -1497) {
            user.setEcus(user.getEcus() - 3);
            userService.update(user.getId(), user);

            rabbit.setClean(true);
            return rabbitRepository.save(rabbit);
        } else {
            throw new RuntimeException(
                "Pas assez d'écus pour nettoyer le lapin !"
            );
        }
    }

    public Rabbit healRabbit(Long rabbitId, Long userId) {
        Rabbit rabbit = findById(rabbitId);
        User user = userService.findById(userId);

        if (user.getEcus() >= -1494) {
            user.setEcus(user.getEcus() - 6);
            userService.update(user.getId(), user);

            rabbit.setHealthy(true);
            return rabbitRepository.save(rabbit);
        } else {
            throw new RuntimeException(
                "Pas assez d'écus pour soigner le lapin !"
            );
        }
    }

    public void processEndOfDay(Long userId) {
        User user = userService.findById(userId);
        if(user.getHibernation() == false){
            List<Rabbit> userRabbits = rabbitRepository.findByUserId(userId);

            long adultCount = userRabbits
                    .stream()
                    .filter(r -> r.getRabbitType() == Rabbit.RabbitTypeEnum.lapin)
                    .count();
            long babyCount = userRabbits
                    .stream()
                    .filter(r -> r.getRabbitType() == Rabbit.RabbitTypeEnum.lapereau)
                    .count();

            for (Rabbit rabbit : userRabbits) {
                if (!rabbit.getClean() || !rabbit.getHealthy()) {
                    if (Math.random() > 0.5) {
                        rabbitRepository.delete(rabbit);
                        if (
                                rabbit.getRabbitType() == Rabbit.RabbitTypeEnum.lapin
                        ) adultCount--;
                        else babyCount--;
                        continue;
                    }
                }

                if (!rabbit.getFedToday()) {
                    rabbitRepository.delete(rabbit);
                    if (
                            rabbit.getRabbitType() == Rabbit.RabbitTypeEnum.lapin
                    ) adultCount--;
                    else babyCount--;
                    continue;
                }

                if (!rabbit.getWateredToday()) {
                    continue;
                } else {
                    rabbit.setAge(rabbit.getAge() + 1);

                    if (
                            rabbit.getRabbitType() == Rabbit.RabbitTypeEnum.lapereau &&
                                    rabbit.getAge() >= 30
                    ) {
                        if (adultCount < 50) {
                            rabbit.setRabbitType(Rabbit.RabbitTypeEnum.lapin);
                            rabbit.setGender(
                                    Math.random() > 0.5
                                            ? Animal.AnimalGender.M
                                            : Animal.AnimalGender.F
                            );
                            adultCount++;
                            babyCount--;
                        }
                    }
                }

                rabbit.setFedToday(false);
                rabbit.setWateredToday(false);
                rabbit.setClean(false); // devient sale le lendemain

                rabbitRepository.save(rabbit);
            }
            handleReproduction(userId, adultCount, babyCount);
        }
    }

    private void handleReproduction(
        Long userId,
        long currentAdults,
        long currentBabies
    ) {
        List<Rabbit> adults = rabbitRepository.findByUserIdAndRabbitType(
            userId,
            Rabbit.RabbitTypeEnum.lapin
        );

        long males = adults
            .stream()
            .filter(r -> r.getGender() == Animal.AnimalGender.M)
            .count();
        long females = adults
            .stream()
            .filter(r -> r.getGender() == Animal.AnimalGender.F)
            .count();

        if (males > 0 && females > 0) {
            int newBabiesCount = (int) (Math.min(males, females) * 3);

            for (int i = 0; i < newBabiesCount; i++) {
                if (currentBabies >= 50) break;

                Rabbit baby = new Rabbit();
                baby.setUserId(userId);
                baby.setRabbitType(Rabbit.RabbitTypeEnum.lapereau);
                baby.setAge(0);
                baby.setGender(null);
                baby.setClean(true);
                baby.setHealthy(true);
                baby.setFedToday(false);
                baby.setWateredToday(false);
                baby.setName("Baby-" + UUID.randomUUID().toString().substring(0, 5));
                rabbitRepository.save(baby);
                currentBabies++;
            }
        }
    }
}
