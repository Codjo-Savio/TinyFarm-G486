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
        // Feeding costs 3 ecus and is mandatory for survival at end of day.
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
        // Healing costs 6 ecus and immediately restores healthy state.
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
        // End-of-day enforces survival, growth to adulthood, and daily state reset rules.
        User user = userService.findById(userId);
        if(Boolean.FALSE.equals(user.getHibernation())){
            RabbitPopulationCounts counts = processAllRabbitsForEndOfDay(userId);
            handleReproduction(userId, counts.adults(), counts.babies());
        }
    }

    private RabbitPopulationCounts processAllRabbitsForEndOfDay(Long userId) {
        // Apply rabbit daily lifecycle and keep population counters in sync for reproduction rules.
        List<Rabbit> userRabbits = rabbitRepository.findByUserId(userId);
        long adultCount = userRabbits.stream().filter(this::isAdultRabbit).count();
        long babyCount = userRabbits.stream().filter(r -> !isAdultRabbit(r)).count();

        for (Rabbit rabbit : userRabbits) {
            if (isRabbitDeletedByHealthOrCleanliness(rabbit) || isRabbitDeletedByMissingFood(rabbit)) {
                if (isAdultRabbit(rabbit)) {
                    adultCount--;
                } else {
                    babyCount--;
                }
                continue;
            }

            if (rabbit.getWateredToday()) {
                rabbit.setAge(rabbit.getAge() + 1);
                if (tryPromoteBabyToAdult(rabbit, adultCount)) {
                    adultCount++;
                    babyCount--;
                }
            } else {
                continue;
            }

            resetDailyRabbitState(rabbit);
            rabbitRepository.save(rabbit);
        }

        return new RabbitPopulationCounts(adultCount, babyCount);
    }

    private boolean isRabbitDeletedByHealthOrCleanliness(Rabbit rabbit) {
        // Unhealthy or dirty rabbits have a 50% chance to die at end of day.
        if ((!rabbit.getClean() || !rabbit.getHealthy()) && Math.random() > 0.5) {
            rabbitRepository.delete(rabbit);
            return true;
        }
        return false;
    }

    private boolean isRabbitDeletedByMissingFood(Rabbit rabbit) {
        // Not feeding a rabbit during the day always causes death.
        if (!rabbit.getFedToday()) {
            rabbitRepository.delete(rabbit);
            return true;
        }
        return false;
    }

    private boolean tryPromoteBabyToAdult(Rabbit rabbit, long adultCount) {
        // Babies become adults at age 30 if adult capacity (50) is not exceeded.
        if (rabbit.getRabbitType() != Rabbit.RabbitTypeEnum.lapereau || rabbit.getAge() < 30) {
            return false;
        }
        if (adultCount >= 50) {
            return false;
        }
        rabbit.setRabbitType(Rabbit.RabbitTypeEnum.lapin);
        rabbit.setGender(Math.random() > 0.5 ? Animal.AnimalGender.M : Animal.AnimalGender.F);
        return true;
    }

    private boolean isAdultRabbit(Rabbit rabbit) {
        return rabbit.getRabbitType() == Rabbit.RabbitTypeEnum.lapin;
    }

    private void resetDailyRabbitState(Rabbit rabbit) {
        // Daily action flags are reset for the next game day.
        rabbit.setFedToday(false);
        rabbit.setWateredToday(false);
        rabbit.setClean(false);
    }

    private record RabbitPopulationCounts(long adults, long babies) {}

    private void handleReproduction(
        Long userId,
        long currentAdults,
        long currentBabies
    ) {
        // Reproduction requires at least one adult male and female; each pair can generate 3 babies.
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
