package com.api.tinyfarm.service;

import com.api.tinyfarm.model.Animal;
import com.api.tinyfarm.model.Rabbit;
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
public class RabbitServiceTest {
    @Autowired
    RabbitService rabbitService;
    @Autowired
    AnimalService animalService;
    @Autowired
    UserRepository userRepository;

    @BeforeEach
    void setup(){
        rabbitService.deleteAllRabbits();
        animalService.deleteAllAnimals();
        userRepository.deleteAll();
    }

    @Test
    void shouldCreateRabbit(){
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
    void shouldReturnAllRabbits(){
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
    void shouldDeleteRabbit(){
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
