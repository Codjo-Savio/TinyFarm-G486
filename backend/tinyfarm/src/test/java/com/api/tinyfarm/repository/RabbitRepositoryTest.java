package com.api.tinyfarm.repository;

import com.api.tinyfarm.model.Animal;
import com.api.tinyfarm.model.Rabbit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class RabbitRepositoryTest {

    @Autowired
    RabbitRepository rabbitRepository;

    @BeforeEach
    void setUp() {
        rabbitRepository.deleteAll();
    }

    @Test
    void shouldSaveAndFindRabbit() {
        Rabbit rabbit = new Rabbit();
        rabbit.setUserId(1L);
        rabbit.setClean(true);
        rabbit.setHealthy(true);
        rabbit.setAge(1);
        rabbit.setWeight(2.5f);
        rabbit.setGender(Animal.AnimalGender.M);
        rabbit.setName("Bugs");
        rabbit.setRabbitType(Rabbit.RabbitTypeEnum.lapin);

        Rabbit saved = rabbitRepository.save(rabbit);

        assertNotNull(saved.getId());
        assertEquals("Bugs", saved.getName());
    }

    @Test
    void shouldFindByName() {
        Rabbit rabbit = new Rabbit();
        rabbit.setUserId(1L);
        rabbit.setClean(true);
        rabbit.setHealthy(true);
        rabbit.setAge(1);
        rabbit.setWeight(2.5f);
        rabbit.setGender(Animal.AnimalGender.M);
        rabbit.setName("Roger");
        rabbit.setRabbitType(Rabbit.RabbitTypeEnum.lapin);

        rabbitRepository.save(rabbit);

        Optional<Rabbit> found = rabbitRepository.findByName("Roger");
        assertTrue(found.isPresent());
        assertEquals("Roger", found.get().getName());
    }

    @Test
    void shouldFindByRabbitType() {
        Rabbit rabbit = new Rabbit();
        rabbit.setUserId(1L);
        rabbit.setClean(true);
        rabbit.setHealthy(true);
        rabbit.setAge(1);
        rabbit.setWeight(1.0f);
        rabbit.setGender(Animal.AnimalGender.F);
        rabbit.setName("Thumper");
        rabbit.setRabbitType(Rabbit.RabbitTypeEnum.lapereau);

        rabbitRepository.save(rabbit);

        List<Rabbit> lapereaux = rabbitRepository.findByRabbitType(Rabbit.RabbitTypeEnum.lapereau);
        assertFalse(lapereaux.isEmpty());
        assertEquals("Thumper", lapereaux.get(0).getName());
        assertEquals(Rabbit.RabbitTypeEnum.lapereau, lapereaux.get(0).getRabbitType());
    }

    @Test
    void shouldDeleteRabbit() {
        Rabbit rabbit = new Rabbit();
        rabbit.setUserId(1L);
        rabbit.setClean(true);
        rabbit.setHealthy(true);
        rabbit.setAge(3);
        rabbit.setWeight(4.0f);
        rabbit.setGender(Animal.AnimalGender.M);
        rabbit.setName("Panpan");
        rabbit.setRabbitType(Rabbit.RabbitTypeEnum.lapin);

        Rabbit saved = rabbitRepository.save(rabbit);
        rabbitRepository.deleteById(saved.getId());

        Optional<Rabbit> found = rabbitRepository.findById(saved.getId());
        assertFalse(found.isPresent());
    }
}
