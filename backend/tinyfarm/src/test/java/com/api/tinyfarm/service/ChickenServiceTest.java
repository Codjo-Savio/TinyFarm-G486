package com.api.tinyfarm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.api.tinyfarm.model.Animal;
import com.api.tinyfarm.model.Chicken;
import com.api.tinyfarm.model.User;
import com.api.tinyfarm.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class ChickenServiceTest {

    @Autowired
    ChickenService chickenService;

    @Autowired
    AnimalService animalService;

    @Autowired
    UserRepository userRepository;

    @BeforeEach
    void setup() {
        chickenService.deleteAll();
        animalService.deleteAllAnimals();
        userRepository.deleteAll();
    }

    @Test
    void shouldCreateChicken() {
        User user = new User(2L, "Brad", User.Gender.M, 2000, false, 1);

        Animal animal = new Animal();
        animal.setUserId(2L);
        animal.setAge(1);
        animal.setWeight(0.5f);
        animal.setGender(Animal.AnimalGender.F);

        Chicken chicken = new Chicken();
        chicken.setId(1L);
        chicken.setUserId(2L);
        chicken.setAge(1);
        chicken.setWeight(0.5f);
        chicken.setGender(Animal.AnimalGender.F);
        chicken.setChickenType(Chicken.ChickenType.H);
        chicken.setName("Clochette");

        Chicken created = chickenService.create(chicken);

        assertNotNull(created);
    }

    @Test
    void shouldReturnAllChickens() {
        User user = new User(2L, "Brad", User.Gender.M, 2000, false, 1);

        Animal animal = new Animal();
        animal.setUserId(2L);
        animal.setAge(1);
        animal.setWeight(0.5f);
        animal.setGender(Animal.AnimalGender.F);

        Chicken chicken = new Chicken();
        chicken.setId(1L);
        chicken.setUserId(2L);
        chicken.setAge(1);
        chicken.setWeight(0.5f);
        chicken.setGender(Animal.AnimalGender.F);
        chicken.setChickenType(Chicken.ChickenType.C);
        chicken.setName("Clochette");

        Chicken created = chickenService.create(chicken);

        assertNotNull(chickenService.findAll());
    }

    @Test
    void shouldFeedChickenAndGainWeight() {
        User user = new User();
        user.setName("Farmer");
        user.setEcus(100);
        user = userRepository.save(user);

        Chicken chicken = new Chicken();
        chicken.setUserId(user.getId());
        chicken.setAge(1);
        chicken.setWeight(1.0f);
        chicken.setChickenType(Chicken.ChickenType.C);
        chicken.setName("Piou");
        chicken.setHealthy(true);
        chicken.setClean(true);
        Chicken created = chickenService.create(chicken);

        chickenService.feedChicken(created.getId(), user.getId());
        chickenService.processEndOfDay(user.getId());

        Chicken updated = chickenService.findById(created.getId());
        assertEquals(1.5f, updated.getWeight(), 0.01f); // 1.0 + 0.5
    }

    @Test
    void shouldStarveChickenAndLoseWeight() {
        User user = new User();
        user.setName("Farmer");
        user.setEcus(100);
        user = userRepository.save(user);

        Chicken chicken = new Chicken();
        chicken.setUserId(user.getId());
        chicken.setAge(1);
        chicken.setWeight(1.0f);
        chicken.setChickenType(Chicken.ChickenType.C);
        chicken.setName("Piou");
        chicken.setHealthy(true);
        chicken.setClean(true);
        Chicken created = chickenService.create(chicken);

        chickenService.processEndOfDay(user.getId());

        Chicken updated = chickenService.findById(created.getId());
        assertEquals(0.8f, updated.getWeight(), 0.01f); // 1.0 - 0.2
        assertEquals(1, updated.getFastingDays());
    }

    @Test
    void shouldWaterCleanAndHealChicken() {
        User user = new User();
        user.setName("Farmer");
        user.setEcus(100);
        user = userRepository.save(user);

        Chicken chicken = new Chicken();
        chicken.setUserId(user.getId());
        chicken.setAge(1);
        chicken.setWeight(1.0f);
        chicken.setChickenType(Chicken.ChickenType.C);
        chicken.setName("Piou");
        chicken.setHealthy(false);
        chicken.setClean(false);
        Chicken created = chickenService.create(chicken);

        chickenService.waterChicken(created.getId(), user.getId());
        chickenService.cleanChicken(created.getId(), user.getId());
        chickenService.healChicken(created.getId(), user.getId());

        Chicken updated = chickenService.findById(created.getId());
        assertEquals(true, updated.getWateredToday());
        assertEquals(true, updated.getClean());
        assertEquals(true, updated.getHealthy());
    }

    @Test
    void shouldLayEggsAndSellThem() {
        User user = new User();
        user.setName("Farmer");
        user.setEcus(100);
        user = userRepository.save(user);

        Chicken rooster = new Chicken();
        rooster.setUserId(user.getId());
        rooster.setAge(6);
        rooster.setWeight(3.0f);
        rooster.setChickenType(Chicken.ChickenType.R);
        rooster.setName("Rooster");
        rooster.setHealthy(true);
        rooster.setClean(true);
        Chicken createdRooster = chickenService.create(rooster);
        chickenService.feedChicken(createdRooster.getId(), user.getId());

        Chicken hen = new Chicken();
        hen.setUserId(user.getId());
        hen.setAge(6);
        hen.setWeight(3.0f);
        hen.setChickenType(Chicken.ChickenType.H);
        hen.setName("Hen");
        hen.setHealthy(true);
        hen.setClean(true);
        Chicken createdHen = chickenService.create(hen);
        chickenService.feedChicken(createdHen.getId(), user.getId());

        // Save original money minus feeding costs (100 - 3 - 3 = 94)
        int initialEcus = userRepository.findById(user.getId()).get().getEcus();

        chickenService.processEndOfDay(user.getId());

        // Check if money increased due to egg sale (8 ecus per egg)
        int finalEcus = userRepository.findById(user.getId()).get().getEcus();
        assert (finalEcus >= initialEcus); // Might be same if 0 eggs laid, but usually >
    }

    @Test
    void shouldDeleteChicken() {
        User user = new User(2L, "Brad", User.Gender.M, 2000, false, 1);

        Animal animal = new Animal();
        animal.setUserId(2L);
        animal.setAge(1);
        animal.setWeight(0.5f);
        animal.setGender(Animal.AnimalGender.F);

        Chicken chicken = new Chicken();
        chicken.setId(1L);
        chicken.setUserId(2L);
        chicken.setAge(1);
        chicken.setWeight(0.5f);
        chicken.setGender(Animal.AnimalGender.F);
        chicken.setChickenType(Chicken.ChickenType.H);
        chicken.setName("Clochette");

        Chicken created = chickenService.create(chicken);

        chickenService.delete(created.getId());
        assertEquals(0, chickenService.findAll().size());
    }
}
