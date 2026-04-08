package com.api.tinyfarm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.api.tinyfarm.model.Stock;
import com.api.tinyfarm.model.StockId;
import com.api.tinyfarm.model.Transaction;
import com.api.tinyfarm.model.User;
import com.api.tinyfarm.repository.TransactionRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authorization.method.AuthorizeReturnObject;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class StockServiceTest {

    @Autowired
    private StockService stockService;

    private Stock testStock;
    private Long testUserId;
    private Float testNbEcu;
    private Long testProductId;

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionService transactionService;

    @BeforeEach
    void setup() throws Exception {
        stockService.deleteAll();

        testUserId = 1L;
        testProductId = 10L;
        testNbEcu = 1500f;

        testStock = new Stock();
        testStock.setId(new StockId(testUserId, testProductId));
        testStock.setQuantity(1000);
        testStock.setCollectible(false);
        testStock = stockService.create(testStock);
    }

    @Test
    void shouldCreateStock() {
        assertNotNull(testStock);
        assertNotNull(testStock.getId());
        assertEquals(testUserId, testStock.getId().getUid());
        assertEquals(testProductId, testStock.getId().getProductID());
        assertEquals(1000, testStock.getQuantity());
        assertEquals(false, testStock.getCollectible());
    }

    @Test
    void shouldFindById() {
        Stock found = stockService.findById(testUserId, testProductId);

        assertNotNull(found);
        assertEquals(testStock.getId(), found.getId());
        assertEquals(1000, found.getQuantity());
    }

    @Test
    void shouldFindByUser() {
        List<Stock> stocks = stockService.findByUser(testUserId);

        assertNotNull(stocks);
        assertEquals(1, stocks.size());
        assertEquals(testStock.getId(), stocks.get(0).getId());
    }

    @Test
    void shouldFindByProduct() {
        List<Stock> stocks = stockService.findByProduct(testProductId);

        assertNotNull(stocks);
        assertEquals(1, stocks.size());
        assertEquals(testStock.getId(), stocks.get(0).getId());
    }

    @Test
    void shouldFindAll() throws Exception {
        Long secondUserId = 2L;
        Long secondProductId = 20L;

        Stock secondStock = new Stock();
        secondStock.setId(new StockId(secondUserId, secondProductId));
        secondStock.setQuantity(500);
        secondStock.setCollectible(true);
        stockService.create(secondStock);

        List<Stock> stocks = stockService.findAll();

        assertNotNull(stocks);
        assertEquals(2, stocks.size());
    }

    @Test
    void shouldUpdateStock() {
        Stock updatedStock = new Stock();
        updatedStock.setQuantity(2000);
        updatedStock.setCollectible(true);

        Stock updated = stockService.update(
            testUserId,
            testProductId,
            updatedStock
        );

        assertNotNull(updated);
        assertEquals(2000, updated.getQuantity());
        assertEquals(true, updated.getCollectible());
    }

    @Test
    void shouldDeleteStockById() {
        stockService.delete(testUserId, testProductId);

        assertThrows(RuntimeException.class, () ->
            stockService.findById(testUserId, testProductId)
        );
    }

    @Test
    void shouldDeleteByUser() throws Exception {
        Long secondProductId = 20L;

        Stock secondStock = new Stock();
        secondStock.setId(new StockId(testUserId, secondProductId));
        secondStock.setQuantity(300);
        secondStock.setCollectible(false);
        stockService.create(secondStock);

        List<Stock> stocksBefore = stockService.findByUser(testUserId);
        assertEquals(2, stocksBefore.size());

        stockService.deleteByUser(testUserId);

        List<Stock> stocksAfter = stockService.findByUser(testUserId);
        assertEquals(0, stocksAfter.size());
    }

    @Test
    void shouldDeleteAll() {
        assertEquals(1, stockService.findAll().size());

        stockService.deleteAll();

        assertEquals(0, stockService.findAll().size());
    }

    @Test
    void shouldModifyStockByBuying() throws Exception {
        // Création d'un User Seller
        User seller = new User();
        seller.setEmail("seller@sellingisfun.com");
        seller.setGender(User.Gender.M);
        seller.setHibernation(false);
        seller.setLevel(10);
        seller.setName("Jean");
        seller.setEcus(0F);

        userService.create(seller);

        Long sellerId = userService
            .findByEmail("seller@sellingisfun.com")
            .getId();

        // Remove the stock created in setUp for testUserId and create one for the seller
        stockService.deleteByUser(testUserId);

        Stock sellerStock = new Stock();
        sellerStock.setId(new StockId(sellerId, testProductId));
        sellerStock.setQuantity(1000);
        sellerStock.setCollectible(false);
        sellerStock = stockService.create(sellerStock);

        // Création d'un User Buyer
        User buyer = new User();
        buyer.setEmail("buyer@lovetobuy.com");
        buyer.setGender(User.Gender.F);
        buyer.setHibernation(false);
        buyer.setLevel(10);
        buyer.setName("Véronica");
        buyer.setEcus(1500F);

        userService.create(buyer);

        Long buyerId = userService.findByEmail("buyer@lovetobuy.com").getId();

        // Création d'une transaction test

        Transaction transaction = new Transaction();
        transaction.setBuyer(buyerId);
        transaction.setSeller(sellerId);
        transaction.setProduct(testProductId);
        transaction.setQuantity(250);
        transaction.setTotalPrice(500);

        transactionService.create(transaction);

        Long transactionId = transactionService
            .findByBuyer(transaction.getBuyer())
            .getId();

        // Données avant achats :

        // ProductId de l'ancien stock :

        Long pastProductId = sellerStock.getId().getProductID();

        // Quantité du stock :

        int pastQuantity = sellerStock.getQuantity();

        // Nombre d'écu du buyer :

        Float pastEcu = buyer.getEcus();

        // Nombre d'écu du seller :

        Float pastSellerEcu = seller.getEcus();

        // On achète :
        stockService.buy(transactionId);

        // On vérifie les données :

        // Le Stock du vendeur :
        Stock sellerStockFound = stockService.findById(
            sellerId,
            transaction.getProduct()
        );

        // Le Stock du buyer (devrait être créé) :
        Stock buyerStockFound = stockService.findById(
            buyerId,
            transaction.getProduct()
        );

        // Le Buyer :

        User buyerFound = userService.findById(buyerId);

        // Le Seller :

        User sellerFound = userService.findById(sellerId);

        // Test Quantité de produit du vendeur (doit diminuer)
        assertEquals(pastProductId, sellerStockFound.getProductId());
        assertEquals(
            pastQuantity - transaction.getQuantity(),
            sellerStockFound.getQuantity()
        );

        // Test Quantité de produit du buyer (doit augmenter)
        assertEquals(pastProductId, buyerStockFound.getProductId());
        assertEquals(transaction.getQuantity(), buyerStockFound.getQuantity());

        // Test Ecus buyer (doit diminuer)
        assertEquals(
            pastEcu - transaction.getTotalPrice(),
            buyerFound.getEcus()
        );

        // Test Ecus seller (doit augmenter)
        assertEquals(
            pastSellerEcu + transaction.getTotalPrice(),
            sellerFound.getEcus()
        );
    }

    @Test
    void shouldModifyStockBySelling() {}
}
