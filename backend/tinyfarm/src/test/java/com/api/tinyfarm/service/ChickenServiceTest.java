package com.api.tinyfarm.service;

import com.api.tinyfarm.model.Animal;
import com.api.tinyfarm.model.Chicken;
import com.api.tinyfarm.model.User;
import com.api.tinyfarm.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
    void setup(){
        chickenService.deleteAll();
        animalService.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldCreateChicken(){
        User user = new User(2L, "Brad", User.Gender.M, 2000, 1);

        Animal animal = new Animal();
        animal.setUserId(2);
        animal.setAge(1);
        animal.setWeight(0.5);
        animal.setGender(AnimalGender.F);

        Chicken chicken = new Chicken();
        chicken.setId(1);
        chicken.setChickenType(ChickenType.poussin);
        chicken.setName("Clochette");

        Chicken created = chickenService.create(chicken);

        assertNotNull(created);
    }

    @Test
    void shouldReturnAllChickens(){
        User user = new User(2L, "Brad", User.Gender.M, 2000, 1);

        Animal animal = new Animal();
        animal.setUserId(2);
        animal.setAge(1);
        animal.setWeight(0.5);
        animal.setGender(AnimalGender.F);

        Chicken chicken = new Chicken();
        chicken.setId(1);
        chicken.setChickenType(ChickenType.poussin);
        chicken.setName("Clochette");

        Chicken created = chickenService.create(chicken);

        assertNotNull(chickenService.getAll());
    }

    @Test
    void shouldDeleteChicken(){
        User user = new User(2L, "Brad", User.Gender.M, 2000, 1);

        Animal animal = new Animal();
        animal.setUserId(2);
        animal.setAge(1);
        animal.setWeight(0.5);
        animal.setGender(AnimalGender.F);

        Chicken chicken = new Chicken();
        chicken.setId(1);
        chicken.setChickenType(ChickenType.poussin);
        chicken.setName("Clochette");

        Chicken created = chickenService.create(chicken);

        assertEquals(0, chickenService.deleteAll());
    }


}
