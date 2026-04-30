package com.api.tinyfarm.service;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

import com.api.tinyfarm.model.*;
import com.api.tinyfarm.repository.CooperativeRepository;
import com.api.tinyfarm.repository.ProductRepository;
import com.api.tinyfarm.repository.RabbitRepository;
import com.api.tinyfarm.repository.ChickenRepository;
import com.api.tinyfarm.repository.StockRepository;
import com.api.tinyfarm.utils.RandomNameProvider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.api.tinyfarm.repository.UserRepository;

@Service
public class CooperativeService {
    private static final float AUTHORIZED_OVERDRAFT_FLOOR = -1500f;

    @Autowired
    private CooperativeRepository cooperativeRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private RabbitRepository rabbitRepository;
    @Autowired
    private ChickenRepository chickenRepository;

    public Integer getMediumPriceForProduct(String description) {
        List<Float> prices = new ArrayList<>();

        for (Cooperative coop : cooperativeRepository.findAll()) {
            for (Product product : productRepository.findByDescription(description)) {
                if (product.getId().equals(coop.getProductId())) {
                    if (coop.getPrice() != null) {
                        prices.add(coop.getPrice());
                    }
                }
            }
        }
        if (prices.isEmpty()) {
            return null;
        }
        Float totalPrices = 0f;
        for (Float price : prices) {
            totalPrices += price;
        }
        return (int) (totalPrices / prices.size());
    }

    public HashMap<Long, Float> getAvailableProducts() {
        HashMap<Long, Float> productPrices = new HashMap<>();
        Map<Long, Float> totalPricesByProductId = new HashMap<>();
        Map<Long, Integer> countsByProductId = new HashMap<>();

        List<Cooperative> cooperatives = cooperativeRepository.findAll();
        for (Cooperative coop : cooperatives) {
            Long productId = coop.getProductId();
            Float price = coop.getPrice();

            if (productId == null || price == null) {
                continue;
            }

            totalPricesByProductId.merge(productId, price, Float::sum);
            countsByProductId.merge(productId, 1, Integer::sum);
        }

        for (Map.Entry<Long, Float> entry : totalPricesByProductId.entrySet()) {
            Long productId = entry.getKey();
            Integer count = countsByProductId.get(productId);

            if (productId == null || count == null || count == 0) {
                continue;
            }

            Float averagePrice = entry.getValue() / count;
            productPrices.put(productId, averagePrice);
        }

        return productPrices;
    }

    public void deleteLessExpensiveWithDescription(Long idBuyer, String description) {
        List<Cooperative> cooperatives = cooperativeRepository.findAll();
        List<Product> products = productRepository.findByDescription(description);
        Long uid = null;
        Long pid = null;

        for (Cooperative coop : cooperatives) {
            for (Product product : products) {
                if (!product.getId().equals(coop.getProductId()))
                    continue;
                if (!product.getDescription().equals(description))
                    continue;

                if (uid == null) {
                    uid = coop.getUserId();
                    pid = coop.getProductId();
                    break;
                }
            }
        }

        if (uid == null)
            return;

        User sellerUser = userRepository.findById(uid).orElse(null);
        User buyerUser = userRepository.findById(idBuyer).orElse(null);
        if (sellerUser == null || buyerUser == null)
            return;

        Integer mediumPrice = getMediumPriceForProduct(description);
        if (mediumPrice == null) {
            return;
        }
        if (buyerUser.getEcus() - mediumPrice < AUTHORIZED_OVERDRAFT_FLOOR) {
            throw new RuntimeException("Écus insuffisants pour effectuer l'achat");
        }

        sellerUser.setEcus(sellerUser.getEcus() + mediumPrice);
        buyerUser.setEcus(buyerUser.getEcus() - mediumPrice);

        userRepository.save(sellerUser);
        userRepository.save(buyerUser);

        cooperativeRepository.deleteByCooperativeIdUserIdAndCooperativeIdProductId(uid, pid);
    }

    public Float sellToCooperative(Long sellerId, Long productId, Integer quantity) {
        validatePositiveQuantity(quantity);
        Stock sellerStock = getSellerStockOrThrow(sellerId, productId);
        ensureStockQuantity(sellerStock, quantity);

        Product product = getProductOrThrow(productId);
        float totalPrice = resolveCooperativeUnitPrice(product) * quantity;

        User seller = getUserOrThrow(sellerId, "Utilisateur introuvable : " + sellerId);
        debitOrCreditSellerStockAndEcus(sellerStock, seller, quantity, totalPrice);
        return totalPrice;
    }

    public void buyFromCooperative(
            Long buyerId,
            Long sellerId,
            Long productId,
            Integer quantity) {
        validatePositiveQuantity(quantity);
        Long resolvedBuyerId = resolveAuthenticatedBuyerId(buyerId);

        List<Cooperative> offers = getSortedAvailableOffers(productId);
        ensureRequestedQuantityAvailable(offers, quantity);
        float averageUnitPrice = computeAverageUnitPrice(offers);

        User buyer = getUserOrThrow(resolvedBuyerId, "Acheteur introuvable");
        debitBuyerForCooperativePurchase(buyer, averageUnitPrice, quantity);

        consumeOffersAndPaySellers(offers, averageUnitPrice, quantity);
        
        Product product = getProductOrThrow(productId);
        if (isRabbitProduct(product)) {
            addRabbitsIfNeeded(resolvedBuyerId, productId, quantity);
        }
        if (isChickenProduct(product)) {
            addChickensIfNeeded(resolvedBuyerId, productId, quantity);
        }
    }

    private void validatePositiveQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantité invalide");
        }
    }

    private Stock getSellerStockOrThrow(Long sellerId, Long productId) {
        return stockRepository
                .findById(new StockId(sellerId, productId))
                .orElseThrow(() -> new RuntimeException("Stock vendeur introuvable"));
    }

    private void ensureStockQuantity(Stock sellerStock, Integer quantity) {
        if (sellerStock.getQuantity() < quantity) {
            throw new RuntimeException("Stock insuffisant");
        }
    }

    private Product getProductOrThrow(Long productId) {
        return productRepository
                .findById(productId)
                .orElseThrow(() -> new RuntimeException("Produit introuvable : " + productId));
    }

    private User getUserOrThrow(Long userId, String errorMessage) {
        return userRepository
                .findById(userId)
                .orElseThrow(() -> new RuntimeException(errorMessage));
    }

    private void debitOrCreditSellerStockAndEcus(Stock sellerStock, User seller, Integer quantity, float totalPrice) {
        sellerStock.setQuantity(sellerStock.getQuantity() - quantity);
        seller.setEcus(seller.getEcus() + totalPrice);
        stockRepository.save(sellerStock);
        userRepository.save(seller);
    }

    private Long resolveAuthenticatedBuyerId(Long buyerId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User currentUser) {
            return currentUser.getId();
        }
        return buyerId;
    }

    private List<Cooperative> getSortedAvailableOffers(Long productId) {
        List<Cooperative> offers = cooperativeRepository
                .findAllByCooperativeIdProductId(productId)
                .stream()
                .filter(offer -> offer.getQuantity() > 0 && offer.getPrice() != null && offer.getPrice() >= 0)
                .sorted(Comparator.comparing(Cooperative::getPrice))
                .toList();
        if (offers.isEmpty()) {
            throw new RuntimeException("Aucune offre coopérative disponible pour ce produit");
        }
        return offers;
    }

    private void ensureRequestedQuantityAvailable(List<Cooperative> offers, Integer requestedQuantity) {
        // A cooperative purchase is allowed only if the global available quantity can
        // satisfy the request.
        int totalAvailableQuantity = offers.stream().mapToInt(Cooperative::getQuantity).sum();
        if (totalAvailableQuantity < requestedQuantity) {
            throw new RuntimeException("Quantité insuffisante dans les offres coopératives");
        }
    }

    private float computeAverageUnitPrice(List<Cooperative> offers) {
        // Business rule: all buyers pay one unique unit price, equal to the average
        // listing price.
        return (float) offers
                .stream()
                .mapToDouble(Cooperative::getPrice)
                .average()
                .orElseThrow(() -> new RuntimeException("Impossible de calculer le prix moyen"));
    }

    private void debitBuyerForCooperativePurchase(User buyer, float averageUnitPrice, Integer quantity) {
        // Daily purchase limit is shared by all trade channels (cooperative + market).
        float totalPrice = averageUnitPrice * quantity;
        if (buyer.getEcus() - totalPrice < AUTHORIZED_OVERDRAFT_FLOOR) {
            throw new RuntimeException("Écus insuffisants pour effectuer l'achat");
        }

        int remainingPurchases = buyer.getRemainingPurchases() == null ? 12 : buyer.getRemainingPurchases();
        if (remainingPurchases <= 0) {
            throw new RuntimeException("Vous ne pouvez plus effectuer d'achat dans la journée");
        }

        buyer.setEcus(buyer.getEcus() - totalPrice);
        buyer.setRemainingPurchases(remainingPurchases - 1);
        userRepository.save(buyer);
    }

    private void consumeOffersAndPaySellers(List<Cooperative> offers, float averageUnitPrice,
            Integer requestedQuantity) {
        // Business rule: cheapest offers are consumed first, then each selected seller
        // is paid at average price.
        int remainingToBuy = requestedQuantity;
        for (Cooperative offer : offers) {
            if (remainingToBuy == 0) {
                break;
            }

            int soldQuantity = Math.min(offer.getQuantity(), remainingToBuy);
            if (soldQuantity <= 0) {
                continue;
            }

            User seller = getUserOrThrow(offer.getUserId(), "Vendeur introuvable");
            seller.setEcus(seller.getEcus() + (averageUnitPrice * soldQuantity));
            userRepository.save(seller);

            offer.setQuantity(offer.getQuantity() - soldQuantity);
            if (offer.getQuantity() == 0) {
                cooperativeRepository.delete(offer);
            } else {
                cooperativeRepository.save(offer);
            }
            remainingToBuy -= soldQuantity;
        }
    }

    private void addRabbitsIfNeeded(Long buyerId, Long productId, Integer quantity) {
        // Buying rabbit products creates rabbit entities directly for the buyer.
        Product product = getProductOrThrow(productId);
        if (!isRabbitProduct(product)) {
            return;
        }

        Animal.AnimalGender gender = resolveRabbitGender(product);
        for (int i = 0; i < quantity; i++) {
            rabbitRepository.save(getRabbit(buyerId, gender));
        }
    }

    private Animal.AnimalGender resolveRabbitGender(Product product) {
        String description = product.getDescription() == null
                ? ""
                : product.getDescription().toLowerCase(Locale.ROOT);
        if (description.contains("male")) {
            return Animal.AnimalGender.M;
        }
        if (description.contains("female") || description.contains("femelle")) {
            return Animal.AnimalGender.F;
        }
        return null;
    }

    private static Rabbit getRabbit(Long buyerId, Animal.AnimalGender gender) {
        Rabbit rabbit = new Rabbit();
        rabbit.setUserId(buyerId);
        rabbit.setRabbitType(Rabbit.RabbitTypeEnum.lapin);
        rabbit.setAge(30);
        rabbit.setGender(gender);

        // Assign a random rabbit name based on inferred gender.
        rabbit.setName(
                gender == Animal.AnimalGender.M
                        ? RandomNameProvider.getRandomMaleName()
                        : RandomNameProvider.getRandomFemaleName());
        return rabbit;
    }

    private void addChickensIfNeeded(Long buyerId, Long productId, Integer quantity) {
        // Buying chicken products creates chicken entities directly for the buyer.
        Product product = getProductOrThrow(productId);
        if (!isChickenProduct(product)) {
            return;
        }

        Animal.AnimalGender gender = resolveChickenGender(product);
        for (int i = 0; i < quantity; i++) {
            chickenRepository.save(getChicken(buyerId, gender));
        }
    }

    private Animal.AnimalGender resolveChickenGender(Product product) {
        String description = product.getDescription() == null
                ? ""
                : product.getDescription().toLowerCase(Locale.ROOT);
        if (description.contains("male") || description.contains("coq") || description.contains("rooster")) {
            return Animal.AnimalGender.M;
        }
        if (description.contains("female") || description.contains("femelle")) {
            return Animal.AnimalGender.F;
        }
        return null;
    }

    private static Chicken getChicken(Long buyerId, Animal.AnimalGender gender) {
        Chicken chicken = new Chicken();
        chicken.setUserId(buyerId);
        chicken.setAge(4);
        chicken.setGender(gender);

        // Assign a type to the chicken based on infered gender
        chicken.setChickenType(
                gender == Animal.AnimalGender.M
                        ? Chicken.ChickenType.B
                        : Chicken.ChickenType.L);

        // Assign a random chicken name based on inferred gender.
        chicken.setName(
                gender == Animal.AnimalGender.M
                        ? RandomNameProvider.getRandomMaleName()
                        : RandomNameProvider.getRandomFemaleName());
        return chicken;
    }

    private Float resolveCooperativeUnitPrice(Product product) {
        // Cooperative sell-back prices are fixed by product family.
        String description = product.getDescription() == null
                ? ""
                : product.getDescription().toLowerCase(Locale.ROOT);

        if (description.contains("egg") || description.contains("oeuf")) {
            return 8f;
        }
        if (isRabbitProduct(product)) {
            return 25f;
        }
        if (isChickenProduct(product)) {
            return 15f;
        }
        if (description.contains("milk") || description.contains("lait")) {
            return 2f;
        }
        throw new IllegalArgumentException(
                "Prix coopérative introuvable pour ce produit: configurez-le dans la coopérative");
    }

    private boolean isRabbitProduct(Product product) {
        String description = product.getDescription() == null
                ? ""
                : product.getDescription().toLowerCase(Locale.ROOT);
        return description.contains("rabbit") || description.contains("lapin");
    }

    private boolean isChickenProduct(Product product) {
        String description = product.getDescription() == null
                ? ""
                : product.getDescription().toLowerCase(Locale.ROOT);
        return description.contains("chicken")
                || description.contains("poule")
                || description.contains("coq")
                || description.contains("rooster");
    }

    // Cooperative opening hours are defined in AoE timezone (UTC-12).
    private static final ZoneId ZONE = ZoneOffset.ofHours(-12);

    private boolean isBetween(LocalTime t, LocalTime start, LocalTime end) {
        if (start.isBefore(end)) {
            return !t.isBefore(start) && t.isBefore(end);
        } else {
            return !t.isBefore(start) || t.isBefore(end); // time range crosses midnight
        }
    }

    private boolean isWeekend(DayOfWeek day) {
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }

    private boolean isOpenWeekday(LocalTime t) {
        return isBetween(t, LocalTime.of(5, 0), LocalTime.of(14, 0))
                || isBetween(t, LocalTime.of(17, 0), LocalTime.of(20, 0))
                || isBetween(t, LocalTime.of(22, 0), LocalTime.of(3, 0));
    }

    private boolean isOpenWeekend(LocalTime t) {
        return isBetween(t, LocalTime.of(9, 0), LocalTime.of(14, 0))
                || isBetween(t, LocalTime.of(19, 0), LocalTime.of(3, 0));
    }

    public boolean isOpen() {
        // Opening state is computed with AoE timezone (UTC-12), not the server local
        // timezone.
        ZonedDateTime now = ZonedDateTime.now(ZONE);
        LocalTime time = now.toLocalTime();
        DayOfWeek day = now.getDayOfWeek();

        return isWeekend(day) ? isOpenWeekend(time) : isOpenWeekday(time);
    }

    public Cooperative create(Cooperative cooperative) {
        if (cooperative == null) {
            throw new IllegalArgumentException("Offre coopérative manquante");
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null &&
                authentication.getPrincipal() instanceof User currentUser) {
            cooperative.setUserId(currentUser.getId());
        }
        if (cooperative.getUserId() == null) {
            throw new IllegalArgumentException("userId manquant pour la coopérative");
        }
        if (cooperative.getProductId() == null) {
            throw new IllegalArgumentException("productId manquant pour la coopérative");
        }
        if (cooperative.getQuantity() < 0) {
            throw new IllegalArgumentException("Quantité coopérative invalide");
        }
        if (cooperative.getPrice() != null && cooperative.getPrice() < 0) {
            throw new IllegalArgumentException("Prix coopérative invalide");
        }
        syncCooperativeId(cooperative);
        if (cooperativeRepository.existsById(cooperative.getCooperativeId())) {
            throw new IllegalArgumentException("Offre coopérative déjà existante pour cet utilisateur / produit");
        }
        return cooperativeRepository.save(cooperative);
    }

    private void syncCooperativeId(Cooperative cooperative) {
        if (cooperative.getUserId() == null || cooperative.getProductId() == null) {
            return;
        }
        cooperative.setCooperativeId(
                new CooperativeID(cooperative.getUserId(), cooperative.getProductId()));
    }

}
