package com.api.tinyfarm.service;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.api.tinyfarm.model.Cooperative;
import com.api.tinyfarm.model.Product;
import com.api.tinyfarm.model.User;
import com.api.tinyfarm.repository.CooperativeRepository;
import com.api.tinyfarm.repository.ProductRepository;

import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
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

    public Integer getMediumPriceForProduct(String description) {
        List<Product> products = new ArrayList<>();

        for (Cooperative coop : cooperativeRepository.findAll()) {
            for (Product product : productRepository.findByDescription(description)) {
                if (product.getId().equals(coop.getProductId())) {
                    products.add(product);
                }
            }
        }
        if (products.isEmpty()) {
            return null; // No products with the given description
        }
        Float totalPrices = 0f;
        for (Product product : products) {
            totalPrices += product.getPrice();
        }
        return (int) (totalPrices / products.size());
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
        HashMap<String, Product> productMap = new HashMap<>();

        Long uid = null;
        Long pid = null;

        for (Cooperative coop : cooperatives) {
            for (Product product : products) {
                if (!product.getId().equals(coop.getProductId()))
                    continue;
                if (!product.getDescription().equals(description))
                    continue;

                if (uid == null || pid == null) {
                    uid = coop.getUserId();
                    pid = coop.getProductId();
                }
            }
        }

        if (uid == null || pid == null)
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

        cooperativeRepository.deleteByUserIdAndProductId(uid, pid);
    }

    // handling open or closen hours
    private static final ZoneId ZONE = ZoneId.of("Europe/Paris");

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
}
