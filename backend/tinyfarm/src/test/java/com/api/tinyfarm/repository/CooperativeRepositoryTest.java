package com.api.tinyfarm.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.api.tinyfarm.model.Cooperative;
import com.api.tinyfarm.model.CooperativeID;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class CooperativeRepositoryTest {

    @Autowired
    private CooperativeRepository cooperativeRepository;

    @BeforeEach
    void setUp() {
        cooperativeRepository.deleteAll();
    }

    @Test
    void shouldSaveCooperative() {
        Cooperative cooperative = createCooperative(1L, 10L, 25.0f);

        Cooperative saved = cooperativeRepository.save(cooperative);

        assertNotNull(saved.getCooperativeId());
        assertEquals(1L, saved.getUserId());
        assertEquals(10L, saved.getProductId());
        assertEquals(25.0f, saved.getPrice());
    }

    @Test
    void shouldFindByUserId() {
        cooperativeRepository.save(createCooperative(1L, 10L, 25.0f));

        List<Cooperative> found = cooperativeRepository.findByCooperativeIdUserId(1L);

        assertFalse(found.isEmpty());
        assertEquals(10L, found.getFirst().getProductId());
    }

    @Test
    void shouldFindByProductId() {
        cooperativeRepository.save(createCooperative(1L, 10L, 25.0f));

        Optional<Cooperative> found = cooperativeRepository.findByCooperativeIdProductId(10L);

        assertTrue(found.isPresent());
        assertEquals(1L, found.get().getUserId());
    }

    @Test
    void shouldFindByUserIdAndProductId() {
        cooperativeRepository.save(createCooperative(1L, 10L, 25.0f));

        Optional<Cooperative> found =
                cooperativeRepository.findByCooperativeIdUserIdAndCooperativeIdProductId(1L, 10L);

        assertTrue(found.isPresent());
        assertEquals(25.0f, found.get().getPrice());
    }

    @Test
    void shouldDeleteByUserIdAndProductId() {
        cooperativeRepository.save(createCooperative(1L, 10L, 25.0f));

        cooperativeRepository.deleteByCooperativeIdUserIdAndCooperativeIdProductId(1L, 10L);

        assertFalse(
                cooperativeRepository
                        .findByCooperativeIdUserIdAndCooperativeIdProductId(1L, 10L)
                        .isPresent());
    }

    @Test
    void shouldDeleteByUserId() {
        cooperativeRepository.save(createCooperative(1L, 10L, 25.0f));
        cooperativeRepository.save(createCooperative(2L, 20L, 13.0f));

        cooperativeRepository.deleteByCooperativeIdUserId(1L);

        List<Cooperative> cooperatives = cooperativeRepository.findAll();
        assertEquals(1, cooperatives.size());
        assertEquals(2L, cooperatives.getFirst().getUserId());
    }

    private Cooperative createCooperative(Long userId, Long productId, Float price) {
        Cooperative cooperative = new Cooperative();
        cooperative.setCooperativeId(new CooperativeID(userId, productId));
        cooperative.setUserId(userId);
        cooperative.setProductId(productId);
        cooperative.setPrice(price);
        return cooperative;
    }
}
