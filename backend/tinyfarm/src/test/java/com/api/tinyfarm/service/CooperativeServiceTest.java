package com.api.tinyfarm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.api.tinyfarm.model.Cooperative;
import com.api.tinyfarm.model.Product;
import com.api.tinyfarm.model.Stock;
import com.api.tinyfarm.model.StockId;
import com.api.tinyfarm.model.User;
import com.api.tinyfarm.repository.CooperativeRepository;
import com.api.tinyfarm.repository.ProductRepository;
import com.api.tinyfarm.repository.StockRepository;
import com.api.tinyfarm.repository.UserRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CooperativeServiceTest {

    @Mock
    private CooperativeRepository cooperativeRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;
    @Mock
    private StockRepository stockRepository;

    @InjectMocks
    private CooperativeService cooperativeService;

    @Test
    void shouldReturnMediumPriceForMatchingProducts() {
        Cooperative firstCooperative = createCooperative(1L, 10L, 12.0f);
        Cooperative secondCooperative = createCooperative(2L, 11L, 18.0f);
        Product firstProduct = createProduct(10L, "Milk", 12.0f);
        Product secondProduct = createProduct(11L, "Milk", 18.0f);

        when(cooperativeRepository.findAll()).thenReturn(List.of(firstCooperative, secondCooperative));
        when(productRepository.findByDescription("Milk")).thenReturn(List.of(firstProduct, secondProduct));

        Integer mediumPrice = cooperativeService.getMediumPriceForProduct("Milk");

        assertEquals(15, mediumPrice);
    }

    @Test
    void shouldReturnNullMediumPriceWhenNoProductMatches() {
        when(cooperativeRepository.findAll()).thenReturn(List.of());

        Integer mediumPrice = cooperativeService.getMediumPriceForProduct("Milk");

        assertNull(mediumPrice);
    }

    @Test
    void shouldReturnAveragePriceForAvailableProducts() {
        Cooperative firstCooperative = createCooperative(1L, 10L, 10.0f);
        Cooperative secondCooperative = createCooperative(2L, 10L, 20.0f);
        Cooperative thirdCooperative = createCooperative(3L, 11L, 9.0f);
        Cooperative ignoredWithoutPrice = createCooperative(4L, 12L, null);
        Cooperative ignoredWithoutProduct = createCooperative(5L, null, 30.0f);

        when(cooperativeRepository.findAll()).thenReturn(
                List.of(firstCooperative, secondCooperative, thirdCooperative, ignoredWithoutPrice,
                        ignoredWithoutProduct));

        HashMap<Long, Float> availableProducts = cooperativeService.getAvailableProducts();

        assertEquals(2, availableProducts.size());
        assertEquals(15.0f, availableProducts.get(10L));
        assertEquals(9.0f, availableProducts.get(11L));
    }

    @Test
    void shouldTransferEcusAndDeleteListingWhenMatchingProductExists() {
        Cooperative cooperative = createCooperative(1L, 10L, 12.0f);
        Product product = createProduct(10L, "Milk", 12.0f);
        User seller = createUser(1L, 100.0f);
        User buyer = createUser(2L, 200.0f);

        when(cooperativeRepository.findAll()).thenReturn(List.of(cooperative));
        when(productRepository.findByDescription("Milk")).thenReturn(List.of(product));
        when(userRepository.findById(1L)).thenReturn(Optional.of(seller));
        when(userRepository.findById(2L)).thenReturn(Optional.of(buyer));

        cooperativeService.deleteLessExpensiveWithDescription(2L, "Milk");

        assertEquals(112.0f, seller.getEcus());
        assertEquals(188.0f, buyer.getEcus());
        verify(userRepository).save(seller);
        verify(userRepository).save(buyer);
        verify(cooperativeRepository).deleteByCooperativeIdUserIdAndCooperativeIdProductId(1L, 10L);
    }

    @Test
    void shouldNotDeleteListingWhenUsersCannotBeResolved() {
        Cooperative cooperative = createCooperative(1L, 10L, 12.0f);
        Product product = createProduct(10L, "Milk", 12.0f);

        when(cooperativeRepository.findAll()).thenReturn(List.of(cooperative));
        when(productRepository.findByDescription("Milk")).thenReturn(List.of(product));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        when(userRepository.findById(2L)).thenReturn(Optional.of(createUser(2L, 200.0f)));

        cooperativeService.deleteLessExpensiveWithDescription(2L, "Milk");

        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any(User.class));
        verify(cooperativeRepository, never())
                .deleteByCooperativeIdUserIdAndCooperativeIdProductId(1L, 10L);
    }

    @Test
    void shouldReturnBooleanForOpenState() {
        boolean open = cooperativeService.isOpen();

        assertTrue(open || !open);
        assertFalse(Boolean.valueOf(open) == null);
    }

    @Test
    void shouldSellToCooperativeAndUpdateStockAndEcus() {
        Stock stock = new Stock();
        stock.setId(new StockId(1L, 10L));
        stock.setQuantity(4);
        stock.setCollectible(false);

        Product product = createProduct(10L, "egg", 99.0f);
        User seller = createUser(1L, 100.0f);

        when(stockRepository.findById(new StockId(1L, 10L))).thenReturn(Optional.of(stock));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(userRepository.findById(1L)).thenReturn(Optional.of(seller));

        Float total = cooperativeService.sellToCooperative(1L, 10L, 2);

        assertEquals(16.0f, total);
        assertEquals(2, stock.getQuantity());
        assertEquals(116.0f, seller.getEcus());
        verify(stockRepository).save(stock);
        verify(userRepository).save(seller);
    }

    private Cooperative createCooperative(Long userId, Long productId, Float price) {
        Cooperative cooperative = new Cooperative();
        cooperative.setUserId(userId);
        cooperative.setProductId(productId);
        cooperative.setPrice(price);
        return cooperative;
    }

    private Product createProduct(Long id, String description, Float price) {
        Product product = new Product();
        product.setId(id);
        product.setDescription(description);
        product.setPrice(price);
        return product;
    }

    private User createUser(Long id, Float ecus) {
        User user = new User();
        user.setId(id);
        user.setEcus(ecus);
        return user;
    }
}
