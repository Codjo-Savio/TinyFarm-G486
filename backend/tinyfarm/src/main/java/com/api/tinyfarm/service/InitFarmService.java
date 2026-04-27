package com.api.tinyfarm.service;

import com.api.tinyfarm.model.Animal;
import com.api.tinyfarm.model.Chicken;
import com.api.tinyfarm.model.Cooperative;
import com.api.tinyfarm.model.CooperativeID;
import com.api.tinyfarm.model.Cow;
import com.api.tinyfarm.model.Market;
import com.api.tinyfarm.model.MarketID;
import com.api.tinyfarm.model.Product;
import com.api.tinyfarm.model.Rabbit;
import com.api.tinyfarm.model.Stock;
import com.api.tinyfarm.model.StockId;
import com.api.tinyfarm.model.User;
import com.api.tinyfarm.repository.ChickenRepository;
import com.api.tinyfarm.repository.CooperativeRepository;
import com.api.tinyfarm.repository.CowRepository;
import com.api.tinyfarm.repository.MarketRepository;
import com.api.tinyfarm.repository.ProductRepository;
import com.api.tinyfarm.repository.RabbitRepository;
import com.api.tinyfarm.repository.StockRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class InitFarmService {
    private static final Set<String> MARKET_AND_COOP_EXCLUDED_PRODUCTS = Set.of(
        "Sac de nourriture",
        "Seau d'eau",
        "Savon",
        "Seringue"
    );

    private final MarketService marketService;
    private final StockService stockService;
    private final ProductService productService;
    private final RabbitService rabbitService;
    private final ChickenService chickenService;
    private final CowService cowService;
    private final CooperativeService cooperativeService;
    private final ProductRepository productRepository;
    private final StockRepository stockRepository;
    private final MarketRepository marketRepository;
    private final CooperativeRepository cooperativeRepository;
    private final RabbitRepository rabbitRepository;
    private final ChickenRepository chickenRepository;
    private final CowRepository cowRepository;

    public InitFarmService(
        MarketService marketService,
        StockService stockService,
        ProductService productService,
        RabbitService rabbitService,
        ChickenService chickenService,
        CowService cowService,
        CooperativeService cooperativeService,
        ProductRepository productRepository,
        StockRepository stockRepository,
        MarketRepository marketRepository,
        CooperativeRepository cooperativeRepository,
        RabbitRepository rabbitRepository,
        ChickenRepository chickenRepository,
        CowRepository cowRepository
    ) {
        this.marketService = marketService;
        this.stockService = stockService;
        this.productService = productService;
        this.rabbitService = rabbitService;
        this.chickenService = chickenService;
        this.cowService = cowService;
        this.cooperativeService = cooperativeService;
        this.productRepository = productRepository;
        this.stockRepository = stockRepository;
        this.marketRepository = marketRepository;
        this.cooperativeRepository = cooperativeRepository;
        this.rabbitRepository = rabbitRepository;
        this.chickenRepository = chickenRepository;
        this.cowRepository = cowRepository;
    }

    public void initializeFarmForUser(User owner) {
        if (owner == null || owner.getId() == null) {
            throw new IllegalArgumentException("Utilisateur invalide pour l'initialisation");
        }

        Map<String, Product> products = ensureBaseProducts();
        ensureStarterAnimals(owner);
        ensureStarterStock(owner, products);
        ensureStarterMarket(owner, products);
        ensureStarterCooperative(owner, products);
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

    private float defaultUnitPrice(String description) {
        return switch (description) {
            case "Paille" -> 5f;
            case "Botte de foin" -> 10f;
            case "Céréales" -> 8f;
            default -> 1f;
        };
    }

    private void ensureStarterAnimals(User owner) {
        Long userId = owner.getId();
        List<Rabbit> rabbits = rabbitRepository.findByUserId(userId);
        List<Chicken> chickens = chickenRepository.findByUserId(userId);
        List<Cow> cows = cowRepository.findByUserId(userId);

        if (rabbits.stream().noneMatch(r -> r.getRabbitType() == Rabbit.RabbitTypeEnum.lapereau)) {
            Rabbit rabbit = new Rabbit();
            rabbit.setUserId(userId);
            rabbit.setName("Arès");
            rabbit.setRabbitType(Rabbit.RabbitTypeEnum.lapereau);
            rabbit.setAge(0);
            rabbitService.create(rabbit);
        }
        if (rabbits.stream().noneMatch(r ->
            r.getRabbitType() == Rabbit.RabbitTypeEnum.lapin && r.getGender() == Animal.AnimalGender.M)) {
            Rabbit rabbit = new Rabbit();
            rabbit.setUserId(userId);
            rabbit.setName("Hermès");
            rabbit.setRabbitType(Rabbit.RabbitTypeEnum.lapin);
            rabbit.setGender(Animal.AnimalGender.M);
            rabbit.setAge(30);
            rabbitService.create(rabbit);
        }
        if (rabbits.stream().noneMatch(r ->
            r.getRabbitType() == Rabbit.RabbitTypeEnum.lapin && r.getGender() == Animal.AnimalGender.F)) {
            Rabbit rabbit = new Rabbit();
            rabbit.setUserId(userId);
            rabbit.setName("Sélémène");
            rabbit.setRabbitType(Rabbit.RabbitTypeEnum.lapin);
            rabbit.setGender(Animal.AnimalGender.F);
            rabbit.setAge(30);
            rabbitService.create(rabbit);
        }

        if (chickens.stream().noneMatch(c -> c.getChickenType() == Chicken.ChickenType.B)) {
            Chicken chicken = new Chicken();
            chicken.setUserId(userId);
            chicken.setName("Zeus");
            chicken.setChickenType(Chicken.ChickenType.B);
            chicken.setGender(Animal.AnimalGender.M);
            chicken.setAge(30);
            chickenService.create(chicken);
        }
        if (chickens.stream().noneMatch(c -> c.getChickenType() == Chicken.ChickenType.L)) {
            Chicken chicken = new Chicken();
            chicken.setUserId(userId);
            chicken.setName("Nefertiti");
            chicken.setChickenType(Chicken.ChickenType.L);
            chicken.setGender(Animal.AnimalGender.F);
            chicken.setAge(30);
            chickenService.create(chicken);
        }
        if (chickens.stream().noneMatch(c -> c.getChickenType() == Chicken.ChickenType.C)) {
            Chicken chicken = new Chicken();
            chicken.setUserId(userId);
            chicken.setName("Keith");
            chicken.setChickenType(Chicken.ChickenType.C);
            chicken.setAge(0);
            chickenService.create(chicken);
        }

        if (cows.stream().noneMatch(c -> c.getCowType() == Cow.CowType.D)) {
            Cow cow = new Cow();
            cow.setUserId(userId);
            cow.setName("Estia");
            cow.setCowType(Cow.CowType.D);
            cow.setGender(Animal.AnimalGender.F);
            cow.setAge(30);
            cowService.create(cow);
        }
        if (cows.stream().noneMatch(c -> c.getCowType() == Cow.CowType.C)) {
            Cow cow = new Cow();
            cow.setUserId(userId);
            cow.setName("Apollon");
            cow.setCowType(Cow.CowType.C);
            cow.setGender(Animal.AnimalGender.M);
            cow.setAge(30);
            cowService.create(cow);
        }
    }

    private void ensureStarterStock(User owner, Map<String, Product> products) {
        for (Product product : products.values()) {
            StockId id = new StockId(owner.getId(), product.getId());
            if (stockRepository.existsById(id)) {
                continue;
            }
            Stock stock = new Stock();
            stock.setUserId(owner.getId());
            stock.setProductId(product.getId());
            stock.setQuantity(100);
            try {
                stockService.create(stock);
            } catch (Exception e) {
                throw new RuntimeException("Erreur initialisation stock", e);
            }
        }
    }

    private void ensureStarterMarket(User owner, Map<String, Product> products) {
        for (Product product : products.values()) {
            MarketID id = new MarketID(owner.getId(), product.getId());
            if (marketRepository.existsById(id)) {
                continue;
            }
            if (MARKET_AND_COOP_EXCLUDED_PRODUCTS.contains(product.getDescription())) {
                continue;
            }
            Market market = new Market();
            market.setUserId(owner.getId());
            market.setProductId(product.getId());
            market.setQuantity(1);
            market.setUnitPrice(defaultUnitPrice(product.getDescription()));
            marketService.create(market);
        }
    }

    private void ensureStarterCooperative(User owner, Map<String, Product> products) {
        for (Product product : products.values()) {
            CooperativeID id = new CooperativeID(owner.getId(), product.getId());
            if (cooperativeRepository.existsById(id)) {
                continue;
            }
            if (MARKET_AND_COOP_EXCLUDED_PRODUCTS.contains(product.getDescription())) {
                continue;
            }
            Cooperative cooperative = new Cooperative();
            cooperative.setUserId(owner.getId());
            cooperative.setProductId(product.getId());
            cooperative.setQuantity(0);
            cooperative.setPrice(defaultUnitPrice(product.getDescription()));
            cooperativeService.create(cooperative);
        }
    }
}
