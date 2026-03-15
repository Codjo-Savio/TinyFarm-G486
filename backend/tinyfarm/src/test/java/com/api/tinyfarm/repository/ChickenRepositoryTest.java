package com.api.tinyfarm.repository;

import com.api.tinyfarm.model.Chicken;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Optional;
import java.util.List;

@DataJpaTest
@ActiveProfiles("test")
public class ChickenRepositoryTest {

    @Autowired
    private ChickenRepository chickenRepository;

    @BeforeEach
    void setUp() {
        chickenRepository.deleteAll();
    }

    @Test
    void shouldSaveChicken() {
        // ARRANGE — Chicken hérite de Animal, donc on remplit les deux
        Chicken chicken = new Chicken();
        // Champs Animal
        chicken.setClean(true);
        chicken.setHealthy(true);
        chicken.setAge(0);
        chicken.setWeight(0.05f);
        chicken.setAGender(false);
        // Champs Chicken
        chicken.setChickenType("POUSSIN");
        chicken.setFasting(false);

        // ACT
        Chicken saved = chickenRepository.save(chicken);

        // ASSERT
        assertNotNull(saved.getAId()); // PK = a_id
        assertEquals("POUSSIN", saved.getChickenType());
        assertEquals(0.05f, saved.getWeight());
        assertFalse(saved.getFasting());
    }

    @Test
    void shouldFindChickenById() {
        // ARRANGE
        Chicken chicken = new Chicken();
        chicken.setChickenType("POULE");
        chicken.setAge(5);
        chicken.setWeight(2.5f);
        chickenRepository.save(chicken);

        // ACT
        Optional<Chicken> found = chickenRepository.findById(chicken.getAId());

        // ASSERT
        assertTrue(found.isPresent());
        assertEquals("POULE", found.get().getChickenType());
        assertEquals(2.5f, found.get().getWeight());
    }

    @Test
    void shouldFindAllChickens() {
        // ARRANGE
        Chicken c1 = new Chicken();
        c1.setChickenType("COQ");
        c1.setWeight(3.0f);

        Chicken c2 = new Chicken();
        c2.setChickenType("POULE");
        c2.setWeight(2.5f);

        chickenRepository.save(c1);
        chickenRepository.save(c2);

        // ACT
        List<Chicken> chickens = chickenRepository.findAll();

        // ASSERT
        assertEquals(2, chickens.size());
    }

    @Test
    void shouldUpdateChickenWeight() {
        // ARRANGE — poussin qui grandit
        Chicken chicken = new Chicken();
        chicken.setWeight(0.05f);
        chicken.setChickenType("POUSSIN");
        chickenRepository.save(chicken);

        // ACT — il mange, il grossit
        chicken.setWeight(0.55f); // +0.5kg après repas
        Chicken updated = chickenRepository.save(chicken);

        // ASSERT
        assertEquals(0.55f, updated.getWeight());
    }

    @Test
    void shouldDeleteChicken() {
        // ARRANGE
        Chicken chicken = new Chicken();
        chicken.setChickenType("COQ");
        chickenRepository.save(chicken);

        // ACT
        chickenRepository.deleteById(chicken.getAId());

        // ASSERT
        Optional<Chicken> found = chickenRepository.findById(chicken.getAId());
        assertFalse(found.isPresent());
    }
}