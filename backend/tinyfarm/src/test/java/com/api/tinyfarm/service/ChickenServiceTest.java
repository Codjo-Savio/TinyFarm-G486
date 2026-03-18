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
