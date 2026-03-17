package com.api.tinyfarm.repository;

import com.api.tinyfarm.model.Animal;
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
        Chicken chicken = new Chicken();
        // Champs Animal
        chicken.setClean(true);
        chicken.setHealthy(true);
        chicken.setAge(0);
        chicken.setWeight(0.05f);
        chicken.setGender(Animal.AnimalGender.F);

        // Champs Chicken
        chicken.setChickenType(Chicken.ChickenType.C);
        chicken.setName("Clochette");
        chicken.setFasting(false);

        // ACT
        Chicken saved = chickenRepository.save(chicken);

        // ASSERT
        assertNotNull(saved.getId()); // PK = a_id
        assertEquals("C", saved.getChickenType());
        assertEquals(0.05f, saved.getWeight());
        assertFalse(saved.getFasting());
    }

    @Test
    void shouldFindChickenById() {
        // ARRANGE
        Chicken chicken = new Chicken();
        chicken.setChickenType(Chicken.ChickenType.H);
        chicken.setName("Clochette");
        chicken.setAge(5);
        chicken.setWeight(2.5f);
        chickenRepository.save(chicken);

        // ACT
        Optional<Chicken> found = chickenRepository.findById(chicken.getId());

        // ASSERT
        assertTrue(found.isPresent());
        assertEquals("H", found.get().getChickenType());
        assertEquals(2.5f, found.get().getWeight());
    }

    @Test
    void shouldFindAllChickens() {
        // ARRANGE
        Chicken chicken = new Chicken();
        chicken.setChickenType(Chicken.ChickenType.R);
        chicken.setName("Pierre");
        chicken.setWeight(3.0f);

        Chicken anotherChicken = new Chicken();
        anotherChicken.setChickenType(Chicken.ChickenType.H);
        anotherChicken.setName("Clochette");
        anotherChicken.setWeight(2.5f);

        chickenRepository.save(chicken);
        chickenRepository.save(anotherChicken);

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
        chicken.setChickenType(Chicken.ChickenType.C);
        chicken.setName("Clochette");
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
        chicken.setChickenType(Chicken.ChickenType.R);
        chicken.setName("Marc");
        chickenRepository.save(chicken);

        // ACT
        chickenRepository.deleteById(chicken.getId());

        // ASSERT
        Optional<Chicken> found = chickenRepository.findById(chicken.getId());
        assertFalse(found.isPresent());
    }
}