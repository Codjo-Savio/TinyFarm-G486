package com.api.tinyfarm.service;

import com.api.tinyfarm.config.TestSecurityConfig;
import com.api.tinyfarm.model.Animal;
import com.api.tinyfarm.model.Cow;
import com.api.tinyfarm.model.User;
import com.api.tinyfarm.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
public class CowServiceTest {

    @Autowired
    CowService cowService;
    @Autowired
    AnimalService animalService;
    @Autowired
    UserRepository userRepository;

    @BeforeEach
    void setup() {
        cowService.deleteAll();
        animalService.deleteAllAnimals();
        userRepository.deleteAll();
    }

    @Test
    void shouldCreateCow() {
        Cow cow = new Cow();
        cow.setCowType(Cow.CowType.D);
        cow.setName("Marguerite");

        Cow created = cowService.create(cow);

        assertNotNull(created);
        assertEquals(false, created.getMilking());
    }

    @Test
    void shouldReturnAllCows() {
        Cow cow = new Cow();
        cow.setCowType(Cow.CowType.B);
        cow.setName("Bovino");

        cowService.create(cow);

        assertNotNull(cowService.findAll());
    }

    @Test
    void shouldDeleteCow() {
        Cow cow = new Cow();
        cow.setCowType(Cow.CowType.D);
        cow.setName("Marguerite");

        Cow created = cowService.create(cow);

        cowService.delete(created.getId());
        assertEquals(0, cowService.findAll().size());
    }
}
