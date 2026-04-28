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
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantité invalide");
        }

        StockId stockId = new StockId(sellerId, productId);
        Stock sellerStock = stockRepository
            .findById(stockId)
            .orElseThrow(() ->
                new RuntimeException("Stock vendeur introuvable")
            );

        if (sellerStock.getQuantity() < quantity) {
            throw new RuntimeException("Stock insuffisant");
        }

        Product product = productRepository
            .findById(productId)
            .orElseThrow(() ->
                new RuntimeException("Produit introuvable : " + productId)
            );

        Float unitPrice = resolveCooperativeUnitPrice(product);
        Float total = unitPrice * quantity;

        User seller = userRepository
            .findById(sellerId)
            .orElseThrow(() ->
                new RuntimeException("Utilisateur introuvable : " + sellerId)
            );

        sellerStock.setQuantity(sellerStock.getQuantity() - quantity);
        seller.setEcus(seller.getEcus() + total);

        stockRepository.save(sellerStock);
        userRepository.save(seller);

        return total;
    }

    public void buyFromCooperative(
        Long buyerId,
        Long sellerId,
        Long productId,
        Integer quantity
    ) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantité invalide");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User currentUser) {
            buyerId = currentUser.getId();
        }

        // handle cooperative ad
        CooperativeID cooperativeID = new CooperativeID(sellerId, productId);
        Cooperative offer = cooperativeRepository
            .findById(cooperativeID)
            .orElseThrow(() -> new RuntimeException("Offre coopérative introuvable"));

        if (offer.getQuantity() < quantity) {
            throw new RuntimeException("Quantité insuffisante dans l'offre coopérative");
        }
        if (offer.getPrice() == null || offer.getPrice() < 0) {
            throw new RuntimeException("Prix coopérative invalide");
        }

        // handle seller
        User seller = userRepository
            .findById(sellerId)
            .orElseThrow(() -> new RuntimeException("Vendeur introuvable"));
        User buyer = userRepository
            .findById(buyerId)
            .orElseThrow(() -> new RuntimeException("Acheteur introuvable"));

        float totalPrice = offer.getPrice() * quantity;
        if (buyer.getEcus() - totalPrice < AUTHORIZED_OVERDRAFT_FLOOR) {
            throw new RuntimeException("Écus insuffisants pour effectuer l'achat");
        }

        int remainingPurchases = buyer.getRemainingPurchases() == null
            ? 12
            : buyer.getRemainingPurchases();
        if (remainingPurchases <= 0) {
            throw new RuntimeException("Vous ne pouvez plus effectuer d'achat dans la journée");
        }

        // handle buyer
        buyer.setEcus(buyer.getEcus() - totalPrice);
        buyer.setRemainingPurchases(remainingPurchases - 1);
        seller.setEcus(seller.getEcus() + totalPrice);
        userRepository.save(buyer);
        userRepository.save(seller);

        offer.setQuantity(offer.getQuantity() - quantity);
        if (offer.getQuantity() == 0) {
            cooperativeRepository.delete(offer);
        } else {
            cooperativeRepository.save(offer);
        }

        Product product = productRepository
            .findById(productId)
            .orElseThrow(() -> new RuntimeException("Produit introuvable : " + productId));


        // Handle rabbit selling case
        if (isRabbitProduct(product)) {
            // We suppose that the gender is null
            Animal.AnimalGender gender = null;

            // We check the content of the ad and set the gender according to it
            if(product.getDescription().toLowerCase().contains("male")){
                gender = Animal.AnimalGender.M;
            }
            else if(product.getDescription().toLowerCase().contains("female") || product.getDescription().toLowerCase().contains("femelle")){
                gender = Animal.AnimalGender.F;
            }
            for (int i = 0; i < quantity; i++) {
                Rabbit rabbit = getRabbit(buyerId, gender);
                rabbitRepository.save(rabbit);
            }
        }
    }

    private static Rabbit getRabbit(Long buyerId, Animal.AnimalGender gender) {
        Rabbit rabbit = new Rabbit();
        rabbit.setUserId(buyerId);
        rabbit.setRabbitType(Rabbit.RabbitTypeEnum.lapin);
        rabbit.setAge(30);
        rabbit.setGender(gender);

        // Name the rabbit according to its gender
        rabbit.setName(
            gender == Animal.AnimalGender.M
                ? RandomNameProvider.getRandomMaleName()
                : RandomNameProvider.getRandomFemaleName()
        );
        return rabbit;
    }

    private Float resolveCooperativeUnitPrice(Product product) {
        String description = product.getDescription() == null
            ? ""
            : product.getDescription().toLowerCase(Locale.ROOT);

        if (description.contains("egg") || description.contains("oeuf")) {
            return 8f;
        }
        if (description.contains("rabbit") || description.contains("lapin")) {
            return 25f;
        }
        if (description.contains("milk") || description.contains("lait")) {
            return 2f;
        }
        throw new IllegalArgumentException(
            "Prix coopérative introuvable pour ce produit: configurez-le dans la coopérative"
        );
    }

    private boolean isRabbitProduct(Product product) {
        String description = product.getDescription() == null
            ? ""
            : product.getDescription().toLowerCase(Locale.ROOT);
        return description.contains("rabbit") || description.contains("lapin");
    }

    // handling open or closen hours in AoE (UTC-12)
    private static final ZoneId ZONE = ZoneOffset.ofHours(-12);

    private boolean isBetween(LocalTime t, LocalTime start, LocalTime end) {
        if (start.isBefore(end)) {
            return !t.isBefore(start) && t.isBefore(end);
        } else {
            return !t.isBefore(start) || t.isBefore(end); // enjambe minuit
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
        ZonedDateTime now = ZonedDateTime.now(ZONE);
        LocalTime time = now.toLocalTime();
        DayOfWeek day = now.getDayOfWeek();

        return isWeekend(day) ? isOpenWeekend(time) : isOpenWeekday(time);
    }

    public Cooperative create(Cooperative cooperative) {
        if (cooperative == null) {
            throw new IllegalArgumentException("Offre coopérative manquante");
        }
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        if (
                authentication != null &&
                        authentication.getPrincipal() instanceof User currentUser
        ) {
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
