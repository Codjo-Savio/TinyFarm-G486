package com.api.tinyfarm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.api.tinyfarm.model.Animal;
import com.api.tinyfarm.model.Rabbit;
import com.api.tinyfarm.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class RabbitServiceTest {

    @Autowired
    RabbitService rabbitService;

    @Autowired
    AnimalService animalService;

    @Autowired
    UserRepository userRepository;

    @BeforeEach
    void setup() {
        rabbitService.deleteAllRabbits();
        animalService.deleteAllAnimals();
        userRepository.deleteAll();
    }

    @Test
    void shouldFeedRabbitAndSurvive() {
        com.api.tinyfarm.model.User user = new com.api.tinyfarm.model.User();
        user.setName("Farmer");
        user.setEcus(100);
        user = userRepository.save(user);

        Rabbit rabbit = new Rabbit();
        rabbit.setUserId(user.getId());
        rabbit.setClean(true);
        rabbit.setHealthy(true);
        rabbit.setAge(1);
        rabbit.setWeight(2.5f);
        rabbit.setGender(Animal.AnimalGender.M);
        rabbit.setName("Panpan");
        rabbit.setRabbitType(Rabbit.RabbitTypeEnum.lapereau);

        Rabbit created = rabbitService.create(rabbit);

        rabbitService.feedRabbit(created.getId(), user.getId());
        rabbitService.processEndOfDay(user.getId());

        Rabbit updated = rabbitService.findById(created.getId());
        assertNotNull(updated);
    }

    @Test
    void shouldStarveRabbitAndDie() {
        com.api.tinyfarm.model.User user = new com.api.tinyfarm.model.User();
        user.setName("Farmer");
        user.setEcus(100);
        user = userRepository.save(user);

        Rabbit rabbit = new Rabbit();
        rabbit.setUserId(user.getId());
        rabbit.setClean(true);
        rabbit.setHealthy(true);
        rabbit.setAge(1);
        rabbit.setWeight(2.5f);
        rabbit.setGender(Animal.AnimalGender.M);
        rabbit.setName("Panpan");
        rabbit.setRabbitType(Rabbit.RabbitTypeEnum.lapereau);

        Rabbit created = rabbitService.create(rabbit);

        rabbitService.processEndOfDay(user.getId());

        assertEquals(0, rabbitService.findAll().size());
    }

    @Test
    void shouldWaterCleanAndHealRabbit() {
        com.api.tinyfarm.model.User user = new com.api.tinyfarm.model.User();
        user.setName("Farmer");
        user.setEcus(100);
        user = userRepository.save(user);

        Rabbit rabbit = new Rabbit();
        rabbit.setUserId(user.getId());
        rabbit.setClean(false);
        rabbit.setHealthy(false);
        rabbit.setAge(1);
        rabbit.setWeight(2.5f);
        rabbit.setGender(Animal.AnimalGender.M);
        rabbit.setName("Panpan2");
        rabbit.setRabbitType(Rabbit.RabbitTypeEnum.lapereau);

        Rabbit created = rabbitService.create(rabbit);

        rabbitService.waterRabbit(created.getId(), user.getId());
        rabbitService.cleanRabbit(created.getId(), user.getId());
        rabbitService.healRabbit(created.getId(), user.getId());

        Rabbit updated = rabbitService.findById(created.getId());
        assertEquals(true, updated.getWateredToday());
        assertEquals(true, updated.getClean());
        assertEquals(true, updated.getHealthy());
    }

    @Test
    void shouldReproduceRabbits() {
        com.api.tinyfarm.model.User user = new com.api.tinyfarm.model.User();
        user.setName("Farmer");
        user.setEcus(100);
        user = userRepository.save(user);

        Rabbit male = new Rabbit();
        male.setUserId(user.getId());
        male.setClean(true);
        male.setHealthy(true);
        male.setAge(30);
        male.setWeight(2.5f);
        male.setGender(Animal.AnimalGender.M);
        male.setName("Male");
        male.setRabbitType(Rabbit.RabbitTypeEnum.lapin);
        Rabbit createdMale = rabbitService.create(male);
        rabbitService.feedRabbit(createdMale.getId(), user.getId());

        Rabbit female = new Rabbit();
        female.setUserId(user.getId());
        female.setClean(true);
        female.setHealthy(true);
        female.setAge(30);
        female.setWeight(2.5f);
        female.setGender(Animal.AnimalGender.F);
        female.setName("Female");
        female.setRabbitType(Rabbit.RabbitTypeEnum.lapin);
        Rabbit createdFemale = rabbitService.create(female);
        rabbitService.feedRabbit(createdFemale.getId(), user.getId());

        rabbitService.processEndOfDay(user.getId());

        // Should have 2 adults + 3 babies
        assertEquals(5, rabbitService.findAll().size());
    }

    @Test
    void shouldCreateRabbit() {
        Rabbit rabbit = new Rabbit();
        rabbit.setUserId(1L);
        rabbit.setClean(true);
        rabbit.setHealthy(true);
        rabbit.setAge(1);
        rabbit.setWeight(2.5f);
        rabbit.setGender(Animal.AnimalGender.M);
        rabbit.setName("Panpan");
        rabbit.setRabbitType(Rabbit.RabbitTypeEnum.lapereau);

        Rabbit created = rabbitService.create(rabbit);

        assertNotNull(created);
        assertEquals("Panpan", created.getName());
    }

    @Test
    void shouldReturnAllRabbits() {
        Rabbit rabbit = new Rabbit();
        rabbit.setUserId(1L);
        rabbit.setClean(true);
        rabbit.setHealthy(true);
        rabbit.setAge(2);
        rabbit.setWeight(3.5f);
        rabbit.setGender(Animal.AnimalGender.F);
        rabbit.setName("Marguerite");
        rabbit.setRabbitType(Rabbit.RabbitTypeEnum.lapin);

        rabbitService.create(rabbit);

        assertNotNull(rabbitService.findAll());
        assertEquals(1, rabbitService.findAll().size());
    }

    @Test
    void shouldDeleteRabbit() {
        Rabbit rabbit = new Rabbit();
        rabbit.setUserId(1L);
        rabbit.setClean(true);
        rabbit.setHealthy(true);
        rabbit.setAge(1);
        rabbit.setWeight(2.0f);
        rabbit.setGender(Animal.AnimalGender.M);
        rabbit.setName("Jeannot");
        rabbit.setRabbitType(Rabbit.RabbitTypeEnum.lapin);

        Rabbit created = rabbitService.create(rabbit);

        rabbitService.delete(created.getId());
        assertEquals(0, rabbitService.findAll().size());
    }
}
