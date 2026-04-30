package com.api.tinyfarm.service;

import com.api.tinyfarm.model.*;
import com.api.tinyfarm.repository.ChickenRepository;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class ChickenService{

    private final ChickenRepository chickenRepository;
    private final UserService userService;
    private final ProductService productService;
    private final CooperativeService cooperativeService;
    private final StockService stockService;

    public ChickenService(
        ChickenRepository chickenRepository,
        UserService userService,
        ProductService productService,
        CooperativeService cooperativeService,
        StockService stockService
    ) {
        this.chickenRepository = chickenRepository;
        this.userService = userService;
        this.productService = productService;
        this.cooperativeService = cooperativeService;
        this.stockService = stockService;
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

    public List<Chicken> findByConnectedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User currentUser)) {
            throw new RuntimeException("Utilisateur non authentifié");
        }
        return chickenRepository.findByUserId(currentUser.getId());
    }

    public Chicken getByName(String name) {
        return chickenRepository
            .findByName(name)
            .orElseThrow(() ->
                new RuntimeException("Poulet introuvable : " + name)
            );
    }

    public Chicken create(Chicken chicken) {
        if (chicken == null) {
            throw new IllegalArgumentException("Poulet manquant");
        }
        if (chicken.getName() == null || chicken.getName().isBlank()) {
            throw new IllegalArgumentException("Nom du poulet manquant");
        }
        if (chicken.getId() != null && chickenRepository.existsById(chicken.getId())) {
            throw new IllegalArgumentException("Poulet déjà existant : " + chicken.getId());
        }
        if (chicken.getWeight() == null) {
            chicken.setWeight(0.05f); // Birth weight
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

    // --- Daily Actions ---

    public Chicken feedChicken(Long chickenId, Long userId) {
        // Feeding costs 3 ecus and still respects the global authorized overdraft floor.
        Chicken chicken = findById(chickenId);
        User user = userService.findById(userId);

        if (user.getEcus() >= -1497) {
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
        // Watering costs 1 ecu and only updates today's hydration flag.
        Chicken chicken = findById(chickenId);
        User user = userService.findById(userId);

        if (user.getEcus() >= -1499) {
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
        // Cleaning costs 3 ecus and restores cleanliness for the current day only.
        Chicken chicken = findById(chickenId);
        User user = userService.findById(userId);

        if (user.getEcus() >= -1497) {
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
        // Healing costs 6 ecus and resets sickness progression.
        Chicken chicken = findById(chickenId);
        User user = userService.findById(userId);

        if (user.getEcus() >= -1494) {
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

    // --- End of Day ---

    public void processEndOfDay(Long userId) {
        // End-of-day applies survival, growth, reproduction eligibility and daily state reset rules.
        User user = userService.findById(userId);
        if (Boolean.FALSE.equals(user.getHibernation())) {
            long[] breeders = processAllChickensForEndOfDay(userId);
            handleEggs(userId, breeders[0], breeders[1]);
        }
    }

    private long[] processAllChickensForEndOfDay(Long userId) {
        // Apply per-chicken daily lifecycle and return active breeder counters (roosters, hens).
        List<Chicken> userChickens = chickenRepository.findByUserId(userId);
        long activeRoosters = 0;
        long activeHens = 0;

        for (Chicken chicken : userChickens) {
            if (isChickenDeadAfterHealthOrFoodRules(chicken) || isChickenDeadAfterWeightRules(chicken)) {
                continue;
            }

            ageAndEvolveChicken(chicken);
            applyDirtPenalty(chicken);

            if (chicken.getChickenType() == Chicken.ChickenType.B) {
                activeRoosters++;
            } else if (chicken.getChickenType() == Chicken.ChickenType.L) {
                activeHens++;
            }

            resetDailyChickenState(chicken);
            chickenRepository.save(chicken);
        }

        return new long[] {activeRoosters, activeHens};
    }

    private boolean isChickenDeadAfterHealthOrFoodRules(Chicken chicken) {
        // Death can be triggered either by sickness progression or starvation progression.
        return handleHealth(chicken) != 0 || handleFood(chicken) != 0;
    }

    private boolean isChickenDeadAfterWeightRules(Chicken chicken) {
        // Weight is capped, and a chicken dies immediately when weight reaches zero or less.
        if (chicken.getWeight() > 3.5f) {
            chicken.setWeight(3.5f);
        }
        if (chicken.getWeight() <= 0f) {
            chickenRepository.delete(chicken);
            return true;
        }
        return false;
    }

    private void ageAndEvolveChicken(Chicken chicken) {
        // Aging and type evolution are evaluated after survival checks.
        chicken.setAge((chicken.getAge() == null ? 0 : chicken.getAge()) + 1);
        handleType(chicken);
    }

    private void applyDirtPenalty(Chicken chicken) {
        // Dirty breeders lose breeder status for the day.
        if (!chicken.getClean()) {
            if (chicken.getChickenType() == Chicken.ChickenType.L) {
                chicken.setChickenType(Chicken.ChickenType.H);
            } else if (chicken.getChickenType() == Chicken.ChickenType.B) {
                chicken.setChickenType(Chicken.ChickenType.R);
            }
        }
    }

    private void resetDailyChickenState(Chicken chicken) {
        // Daily action flags are always reset at end of day.
        chicken.setFedToday(false);
        chicken.setWateredToday(false);
        chicken.setClean(false);
    }

    // Returns 1 when the chicken dies, 0 otherwise.
    private int handleHealth(Chicken chicken){
        // A sick chicken accumulates sick days and dies on day 4 if not healed.

        int out = 0;

        if (chicken.getHealthy() != null && !chicken.getHealthy()) {
            chicken.setSickDays(
                (chicken.getSickDays() == null
                        ? 0
                        : chicken.getSickDays()) + 1
                );

            // Sick chickens temporarily lose breeder status.
            if (chicken.getChickenType() == Chicken.ChickenType.L) {
                chicken.setChickenType(Chicken.ChickenType.H);
            } else if (chicken.getChickenType() == Chicken.ChickenType.B) {
                chicken.setChickenType(Chicken.ChickenType.R);
            }

            if (chicken.getSickDays() >= 4) {
                chickenRepository.delete(chicken);
                return 1; // chicken dies
            }
        } else {
            chicken.setSickDays(0);
        }

        return out;
    }

    private int handleFood(Chicken chicken){
        // Missing food causes escalating weight loss and death on day 4.

        int out = 0;

        if (chicken.getFedToday() != null && !chicken.getFedToday()) {
            chicken.setFastingDays(
                (chicken.getFastingDays() == null
                        ? 0
                        : chicken.getFastingDays()) + 1
                );

            // Starving chickens temporarily lose breeder status.
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
                return 1; // chicken dies
            }
            chicken.setWeight(chicken.getWeight() - weightLoss);
        } else {
            chicken.setFastingDays(0);
            float weightGain = 0.5f; // Grain
            if (
                chicken.getWateredToday() != null &&
                chicken.getWateredToday()
            ) {
                weightGain += 0.15f; // Water bonus applies only when fed
            }
            chicken.setWeight(chicken.getWeight() + weightGain);
        }

        return out;
    }

    private void handleType(Chicken chicken){
        // Type transitions depend on age, weight and productivity constraints.

        // Breeders revert to non-breeder status if they lose too much weight.
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

        // Transition from chick to young adult at day 4.
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

        // Transition to breeder state requires age and weight thresholds.
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

    int totalEggToReturn = 0;
    private void handleEggs(Long userId, long activeRoosters, long activeHens) {
        // Each rooster can fertilize up to 5 hens; eggs are sold immediately to cooperative.
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

        // Eggs are auto-sold to cooperative at 8 ecus per egg.
        if (totalEggs > 0) {
            // Ensure egg product exists before cooperative sale.
            Product egg = addEggAsProduct();

            // Ensure the stock exists before cooperative sale
            Stock stock = addEggStock(egg, userId, totalEggs);

            cooperativeService.sellToCooperative(userId, egg.getId(), totalEggs);
        }
        totalEggToReturn += totalEggs;
    }

    public int getEggNumber(){
        return totalEggToReturn;
    }

    public Product addEggAsProduct(){
        Product egg = new Product();
        egg.setDescription("Oeuf");
        return productService.create(egg);
    }

    public Stock addEggStock(Product egg, Long userId, Integer totalEggs){
        Stock stock = new Stock();
        try {
            stock.setProductId(egg.getId());
            stock.setQuantity(totalEggs);
            stock.setUserId(userId);
            stockService.create(stock);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        return stock;
    }
}
