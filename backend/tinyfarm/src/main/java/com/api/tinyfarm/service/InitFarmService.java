package com.api.tinyfarm.service;

import com.api.tinyfarm.model.Animal;
import com.api.tinyfarm.model.Chicken;
import com.api.tinyfarm.model.Cow;
import com.api.tinyfarm.model.Product;
import com.api.tinyfarm.model.Rabbit;
import com.api.tinyfarm.model.Stock;
import com.api.tinyfarm.model.StockId;
import com.api.tinyfarm.model.User;
import com.api.tinyfarm.repository.ChickenRepository;
import com.api.tinyfarm.repository.CowRepository;
import com.api.tinyfarm.repository.ProductRepository;
import com.api.tinyfarm.repository.RabbitRepository;
import com.api.tinyfarm.repository.StockRepository;

import java.util.*;
import java.util.stream.Collectors;

import com.api.tinyfarm.utils.RandomNameProvider;
import org.springframework.stereotype.Service;

@Service
public class InitFarmService {
    private static final int STARTER_LAPEREAU_COUNT = 8;
    private static final float MIN_BREEDING_ROOSTER_WEIGHT = 2.5f;
    private static final float MIN_LAYING_HEN_WEIGHT = 2.5f;

    private final StockService stockService;
    private final ProductService productService;
    private final RabbitService rabbitService;
    private final ChickenService chickenService;
    private final CowService cowService;
    private final ProductRepository productRepository;
    private final StockRepository stockRepository;
    private final RabbitRepository rabbitRepository;
    private final ChickenRepository chickenRepository;
    private final CowRepository cowRepository;

    public InitFarmService(
            StockService stockService,
            ProductService productService,
            RabbitService rabbitService,
            ChickenService chickenService,
            CowService cowService,
            ProductRepository productRepository,
            StockRepository stockRepository,
            RabbitRepository rabbitRepository,
            ChickenRepository chickenRepository,
            CowRepository cowRepository) {
        this.stockService = stockService;
        this.productService = productService;
        this.rabbitService = rabbitService;
        this.chickenService = chickenService;
        this.cowService = cowService;
        this.productRepository = productRepository;
        this.stockRepository = stockRepository;
        this.rabbitRepository = rabbitRepository;
        this.chickenRepository = chickenRepository;
        this.cowRepository = cowRepository;
    }

    public void initializeFarmForUser(User owner) {
        // User bootstrap creates mandatory products, starter animals and initial stock.
        if (owner == null || owner.getId() == null) {
            throw new IllegalArgumentException("Utilisateur invalide pour l'initialisation");
        }

        Map<String, Product> products = ensureBaseProducts();
        ensureStarterAnimals(owner);
        ensureStarterStock(owner, products);
    }

    private Map<String, Product> ensureBaseProducts() {
        Map<String, Product> products = new HashMap<>();
        products.put("Paille", ensureProduct("Paille"));
        products.put("Botte de foin", ensureProduct("Botte de foin"));
        products.put("Céréales", ensureProduct("Céréales"));
        products.put("Sac de nourriture", ensureProduct("Sac de nourriture"));
        products.put("Seau d'eau", ensureProduct("Seau d'eau"));
        products.put("Savon", ensureProduct("Savon"));
        products.put("Seringue", ensureProduct("Seringue"));
        return products;
    }

    private Product ensureProduct(String description) {
        List<Product> existing = productRepository.findByDescription(description);
        if (!existing.isEmpty()) {
            return existing.getFirst();
        }

        Product product = new Product();
        product.setDescription(description);
        return productService.create(product);
    }

    private void ensureStarterAnimals(User owner) {
        Long userId = owner.getId();
        List<Rabbit> rabbits = rabbitRepository.findByUserId(userId);
        List<Chicken> chickens = chickenRepository.findByUserId(userId);
        List<Cow> cows = cowRepository.findByUserId(userId);

        ensureStarterRabbits(userId, rabbits);
        ensureStarterChickens(userId, chickens);
        ensureStarterCows(userId, cows);
    }

    private void ensureStarterRabbits(Long userId, List<Rabbit> rabbits) {
        // Starter setup always injects a base population of lapereaux.
        Set<String> usedRabbitNames = rabbits.stream()
                .map(Rabbit::getName)
                .filter(name -> name != null && !name.isBlank())
                .collect(Collectors.toCollection(HashSet::new));
        for (int i = 0; i < STARTER_LAPEREAU_COUNT; i++) {
            Rabbit rabbit = new Rabbit();
            rabbit.setUserId(userId);
            rabbit.setName(RandomNameProvider.getRandomUnknownGenderName(usedRabbitNames));
            rabbit.setRabbitType(Rabbit.RabbitTypeEnum.lapereau);
            rabbit.setAge(0);
            rabbitService.create(rabbit);
        }
    }

    private void ensureStarterChickens(Long userId, List<Chicken> chickens) {
        // Starter flock guarantees one rooster breeder, one laying hen, and one chick.
        if (chickens.stream().noneMatch(c -> c.getChickenType() == Chicken.ChickenType.B)) {
            Chicken chicken = new Chicken();
            chicken.setUserId(userId);
            chicken.setName(RandomNameProvider.getRandomMaleName());
            chicken.setChickenType(Chicken.ChickenType.B);
            chicken.setGender(Animal.AnimalGender.M);
            chicken.setAge(30);
            chicken.setWeight(MIN_BREEDING_ROOSTER_WEIGHT);
            chickenService.create(chicken);
        }
        if (chickens.stream().noneMatch(c -> c.getChickenType() == Chicken.ChickenType.L)) {
            Chicken chicken = new Chicken();
            chicken.setUserId(userId);
            chicken.setName(RandomNameProvider.getRandomFemaleName());
            chicken.setChickenType(Chicken.ChickenType.L);
            chicken.setGender(Animal.AnimalGender.F);
            chicken.setWeight(MIN_LAYING_HEN_WEIGHT);
            chicken.setAge(30);
            chickenService.create(chicken);
        }
        if (chickens.stream().noneMatch(c -> c.getChickenType() == Chicken.ChickenType.C)) {
            Chicken chicken = new Chicken();
            chicken.setUserId(userId);
            chicken.setName(RandomNameProvider.getRandomUnknownGenderName());
            chicken.setChickenType(Chicken.ChickenType.C);
            chicken.setAge(0);
            chickenService.create(chicken);
        }
    }

    private void ensureStarterCows(Long userId, List<Cow> cows) {
        if (cows.isEmpty()) {
            Cow cow = new Cow();
            cow.setUserId(userId);
            cow.setName(RandomNameProvider.getRandomFemaleName());
            cow.setCowType(Cow.CowType.D);
            cow.setGender(Animal.AnimalGender.F);
            cow.setAge(1);
            cowService.create(cow);
        }
    }

    private void ensureStarterStock(User owner, Map<String, Product> products) {
        // Every starter product is initialized with quantity 100 if no stock entry
        // exists yet.
        for (Product product : products.values()) {
            StockId id = new StockId(owner.getId(), product.getId());
            if (stockRepository.existsById(id)) {
                continue;
            }
            Stock stock = new Stock();
            stock.setUserId(owner.getId());
            stock.setProductId(product.getId());
            stock.setQuantity(2);
            try {
                stockService.create(stock);
            } catch (Exception e) {
                throw new RuntimeException("Erreur initialisation stock", e);
            }
        }
    }
}
