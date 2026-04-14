package com.api.tinyfarm.repository;

import com.api.tinyfarm.model.Animal;
import com.api.tinyfarm.model.Cow;
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
public class CowRepositoryTest {

    @Autowired
    private CowRepository cowRepository;

    @BeforeEach
    void setUp() {
        cowRepository.deleteAll();
    }

    @Test
    void shouldSaveCow() {
        // ARRANGE
        Cow cow = new Cow();
        cow.setClean(true);
        cow.setHealthy(true);
        cow.setAge(3);
        cow.setWeight(450.0f);
        cow.setGender(Animal.AnimalGender.F);
        cow.setCowType(Cow.CowType.D);
        cow.setName("Marguerite");
        cow.setMilking(false);

        // ACT
        Cow saved = cowRepository.save(cow);

        // ASSERT
        assertNotNull(saved.getId());
        assertEquals(450.0f, saved.getWeight());
        assertFalse(saved.getMilking());
    }

    @Test
    void shouldFindCowById() {
        // ARRANGE
        Cow cow = new Cow();
        cow.setCowType(Cow.CowType.B);
        cow.setName("Marguerite");
        cow.setAge(4);
        cow.setWeight(500.0f);
        cowRepository.save(cow);

        // ACT
        Optional<Cow> found = cowRepository.findById(cow.getId());

        // ASSERT
        assertTrue(found.isPresent());
        assertEquals(500.0f, found.get().getWeight());
    }

    @Test
    void shouldFindAllCows() {
        // ARRANGE
        Cow cow1 = new Cow();
        cow1.setCowType(Cow.CowType.D);
        cow1.setName("Marguerite");
        cow1.setWeight(450.0f);

        Cow cow2 = new Cow();
        cow2.setCowType(Cow.CowType.B);
        cow2.setName("Bovino");
        cow2.setWeight(600.0f);

        cowRepository.save(cow1);
        cowRepository.save(cow2);

        // ACT
        List<Cow> cows = cowRepository.findAll();

        // ASSERT
        assertEquals(2, cows.size());
    }

    @Test
    void shouldUpdateCowWeight() {
        // ARRANGE
        Cow cow = new Cow();
        cow.setWeight(400.0f);
        cow.setCowType(Cow.CowType.C);
        cow.setName("Veau");
        cowRepository.save(cow);

        // ACT
        cow.setWeight(450.0f);
        Cow updated = cowRepository.save(cow);

        // ASSERT
        assertEquals(450.0f, updated.getWeight());
    }

    @Test
    void shouldDeleteCow() {
        // ARRANGE
        Cow cow = new Cow();
        cow.setCowType(Cow.CowType.D);
        cow.setName("Marguerite");
        cowRepository.save(cow);

        // ACT
        cowRepository.deleteById(cow.getId());

        // ASSERT
        Optional<Cow> found = cowRepository.findById(cow.getId());
        assertFalse(found.isPresent());
    }

    @Test
    void 
}
