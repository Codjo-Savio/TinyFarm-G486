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
import com.api.tinyfarm.repository.StockRepository;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.api.tinyfarm.repository.UserRepository;

@Service
public class CooperativeService {

    @Autowired
    private CooperativeRepository cooperativeRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private StockRepository stockRepository;

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

        sellerUser.setEcus(
                sellerUser.getEcus() +
                        getMediumPriceForProduct(description));

        buyerUser.setEcus(
                buyerUser.getEcus() -
                        getMediumPriceForProduct(description));

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
