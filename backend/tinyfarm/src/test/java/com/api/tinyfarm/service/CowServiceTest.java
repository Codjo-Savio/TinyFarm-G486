package com.api.tinyfarm.service;

import com.api.tinyfarm.model.Cow;
import com.api.tinyfarm.model.User;
import com.api.tinyfarm.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@ActiveProfiles("test")
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

    @Test
    void shouldHayCow() {
        User usr = new User(2L, "Brad", "usertest@gmail.com", User.Gender.M, 2000, false, 1);

        usr = userRepository.save(usr);

        Cow cow = new Cow();
        cow.setCowType(Cow.CowType.D);
        cow.setName("Marguerite");
        cow.setUserId(usr.getId());

        Cow created = cowService.create(cow);

        cowService.hayCow(created.getId(), created.getUserId());
        assertEquals(true, cowService.findById(created.getId()).getHayToday());
    }

    @Test
    void shouldFeedCow() {
        Cow cow = new Cow();
        cow.setCowType(Cow.CowType.D);
        cow.setName("Marguerite");

        Cow created = cowService.create(cow);

        cowService.grassCow(created.getId());
        assertEquals(true, cowService.findById(created.getId()).getFedToday());
    }

    @Test
    void shouldWaterCow() {
        User usr = new User(2L, "Brad", "usertest@gmail.com", User.Gender.M, 2000, false, 1);

        usr = userRepository.save(usr);

        Cow cow = new Cow();
        cow.setCowType(Cow.CowType.D);
        cow.setName("Marguerite");
        cow.setWateredToday(false);
        cow.setUserId(usr.getId());

        Cow created = cowService.create(cow);

        cowService.waterCow(created.getId(), created.getUserId());
        assertEquals(true, cowService.findById(created.getId()).getWateredToday());
    }

    @Test
    void shouldCleanCow() {
        User usr = new User(2L, "Brad", "usertest@gmail.com", User.Gender.M, 2000, false, 1);

        usr = userRepository.save(usr);

        Cow cow = new Cow();
        cow.setCowType(Cow.CowType.D);
        cow.setName("Marguerite");
        cow.setClean(false);
        cow.setUserId(usr.getId());

        Cow created = cowService.create(cow);

        cowService.cleanCow(created.getId(), created.getUserId());
        assertEquals(true, cowService.findById(created.getId()).getClean());
    }

    @Test
    void shouldHealCow() {
        User usr = new User(2L, "Brad", "usertest@gmail.com", User.Gender.M, 2000, false, 1);

        usr = userRepository.save(usr);

        Cow cow = new Cow();
        cow.setCowType(Cow.CowType.D);
        cow.setName("Marguerite");
        cow.setHealthy(false);
        cow.setUserId(usr.getId());

        Cow created = cowService.create(cow);

        cowService.healCow(created.getId(), created.getUserId());
        assertEquals(true, cowService.findById(created.getId()).getHealthy());
    }

    @Test
    void shouldHave8milk() {
        Cow cow = new Cow();
        cow.setCowType(Cow.CowType.D);
        cow.setName("Marguerite");

        Cow created = cowService.create(cow);

        cowService.milking(created.getId());
        assertEquals(8, cowService.findById(created.getId()).getMilk());
    }

    @Test
    void shouldHave16milk() {
        Cow cow = new Cow();
        cow.setCowType(Cow.CowType.D);
        cow.setName("Marguerite");
        cow.setMilk(12);

        Cow created = cowService.create(cow);

        cowService.milking(created.getId());
        assertEquals(16, cowService.findById(created.getId()).getMilk());
    }

    @Test
    void shouldStopMilking() {
        Cow cow = new Cow();
        cow.setCowType(Cow.CowType.D);
        cow.setName("Marguerite");
        cow.setMilk(12);

        Cow created = cowService.create(cow);

        cowService.milking(created.getId());
        assertEquals(false, cowService.findById(created.getId()).getMilking());
    }

    @Test
    void shouldGain9Kg() {
        Cow cow = new Cow();
        cow.setCowType(Cow.CowType.D);
        cow.setName("Marguerite");

        Cow created = cowService.create(cow);

        cowService.handleWeight(created.getId());
        assertEquals(10, cowService.findById(created.getId()).getWeight());
    }

    @Test
    void shouldGain6Kg() {
        Cow cow = new Cow();
        cow.setCowType(Cow.CowType.D);
        cow.setName("Marguerite");
        cow.setHayToday(false);

        Cow created = cowService.create(cow);

        cowService.handleWeight(created.getId());
        assertEquals(7, cowService.findById(created.getId()).getWeight());
    }

    @Test
    void shouldGain8Kg() {
        Cow cow = new Cow();
        cow.setCowType(Cow.CowType.D);
        cow.setName("Marguerite");
        cow.setWateredToday(false);

        Cow created = cowService.create(cow);

        cowService.handleWeight(created.getId());
        assertEquals(9, cowService.findById(created.getId()).getWeight());
    }

    @Test
    void shouldGain5Kg() {
        Cow cow = new Cow();
        cow.setCowType(Cow.CowType.D);
        cow.setName("Marguerite");
        cow.setWateredToday(false);

        Cow created = cowService.create(cow);

        cowService.handleWeight(created.getId());
        assertEquals(6, cowService.findById(created.getId()).getWeight());
    }

    @Test
    void shouldDie() {

        User usr = new User(2L, "Brad", "usertest@gmail.com", User.Gender.M, 2000, false, 1);

        usr = userRepository.save(usr);

        Cow cow = new Cow();
        cow.setCowType(Cow.CowType.D);
        cow.setName("Marguerite");
        cow.setSickDays(3);
        cow.setUserId(usr.getId());

        Cow created = cowService.create(cow);

        cowService.processEndOfDay(usr.getId());
        assertNull(cowService.findAll());
    }
}
