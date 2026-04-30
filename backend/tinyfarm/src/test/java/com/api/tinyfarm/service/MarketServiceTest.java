package com.api.tinyfarm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.api.tinyfarm.model.Market;
import com.api.tinyfarm.model.Product;
import com.api.tinyfarm.model.Stock;
import com.api.tinyfarm.model.StockId;
import com.api.tinyfarm.model.User;
import com.api.tinyfarm.repository.MarketRepository;
import com.api.tinyfarm.repository.ProductRepository;
import com.api.tinyfarm.repository.StockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class MarketServiceTest {

    @Autowired
    private MarketService marketService;
    @Autowired
    private MarketRepository marketRepository;
    @Autowired
    private ProductService productService;
    @Autowired
    private StockService stockService;
    @Autowired
    private UserService userService;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private StockRepository stockRepository;

    private Long productId;

    @BeforeEach
    void setup() {
        marketRepository.deleteAll();
        stockRepository.deleteAll();
        productRepository.deleteAll();

        Product product = new Product();
        product.setDescription("Eau");

        Product savedProduct = productService.create(product);
        productId = savedProduct.getId();

        try {
            Stock stock = new Stock();
            stock.setProductId(productId);
            stock.setQuantity(200);
            stockService.create(stock);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    void shouldCreateMarket() {

        Market market = new Market();
        market.setUserId(1L);
        market.setProductId(productId);
        market.setUnitPrice(25.0f);
        market.setQuantity(100);

        Market created = marketService.create(market);

        assertNotNull(created.getMarketId());
        assertEquals(1L, created.getUserId());
        assertEquals(productId, created.getProductId());
        assertEquals(100, created.getQuantity());
    }

    @Test
    void shouldReturnMarketByProductId() {
        Market market = new Market();
        market.setUserId(1L);
        market.setProductId(productId);
        market.setUnitPrice(25.0f);

        marketService.create(market);

        Market found = marketService.findByProductId(productId);

        assertNotNull(found);
        assertEquals(1L, found.getUserId());
    }

    @Test
    void shouldDeleteMarketByUserIdAndProductId() {
        Market market = new Market();
        market.setUserId(1L);
        market.setProductId(productId);
        market.setUnitPrice(25.0f);
        marketService.create(market);

        marketService.deleteProductById(1L, productId);

        assertEquals(0, marketService.findAll().size());
    }

    @Test
    void shouldDeleteMarketByUserId() {
        Market market = new Market();
        market.setUserId(1L);
        market.setProductId(productId);
        market.setUnitPrice(25.0f);
        marketService.create(market);

        marketService.deleteByID(1L);

        assertEquals(0, marketService.findAll().size());
    }

    @Test
    void shouldKeepMarketAdWhenQuantityRemainsAfterPurchase() {
        User seller = new User();
        seller.setName("Seller Market");
        seller.setEmail("seller_market_partial@test.com");
        seller.setGender(User.Gender.M);
        seller.setEcus(100.0f);
        seller = userService.create(seller);

        User buyer = new User();
        buyer.setName("Buyer Market");
        buyer.setEmail("buyer_market_partial@test.com");
        buyer.setGender(User.Gender.F);
        buyer.setEcus(200.0f);
        buyer = userService.create(buyer);

        Market listing = new Market();
        listing.setUserId(seller.getId());
        listing.setProductId(productId);
        listing.setUnitPrice(13.0f);
        listing.setQuantity(5);
        marketService.create(listing);

        marketService.buyFromMarket(buyer.getId(), seller.getId(), productId, 2);

        Market updatedListing = marketService.findByProductId(productId);
        assertEquals(3, updatedListing.getQuantity());

        Stock updatedBuyerStock = stockRepository.findById(new StockId(buyer.getId(), productId)).orElseThrow();
        assertEquals(2, updatedBuyerStock.getQuantity());
    }
}
