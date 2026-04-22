package com.api.tinyfarm.service;

import com.api.tinyfarm.model.Animal;
import com.api.tinyfarm.model.Chicken;
import com.api.tinyfarm.model.Rabbit;
import com.api.tinyfarm.model.User;
import com.api.tinyfarm.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class EndOfTheDayServiceScenarioTest {

    @Autowired
    private EndOfTheDayService endOfTheDayService;

    @Autowired
    private ChickenService chickenService;

    @Autowired
    private RabbitService rabbitService;

    @Autowired
    private AnimalService animalService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        chickenService.deleteAll();
        rabbitService.deleteAllRabbits();
        animalService.deleteAllAnimals();
        userRepository.deleteAll();
    }

    @Test
    void shouldApplyEndOfDayWithConcreteStateChanges() {
        User user = new User();
        user.setName("Scenario Farmer");
        user.setEmail("scenario-service-1@gmail.com");
        user.setEcus(100F);
        user = userRepository.save(user);

        Chicken chicken = new Chicken();
        chicken.setUserId(user.getId());
        chicken.setName("Piou");
        chicken.setChickenType(Chicken.ChickenType.C);
        chicken.setAge(1);
        chicken.setWeight(1.0f);
        chicken.setHealthy(true);
        chicken.setClean(true);
        chicken.setGender(Animal.AnimalGender.F);
        Chicken savedChicken = chickenService.create(chicken);
        chickenService.feedChicken(savedChicken.getId(), user.getId());
        chickenService.waterChicken(savedChicken.getId(), user.getId());

        Rabbit rabbit = new Rabbit();
        rabbit.setUserId(user.getId());
        rabbit.setName("Panpan");
        rabbit.setRabbitType(Rabbit.RabbitTypeEnum.lapereau);
        rabbit.setAge(10);
        rabbit.setWeight(2.5f);
        rabbit.setHealthy(true);
        rabbit.setClean(true);
        rabbit.setGender(Animal.AnimalGender.M);
        Rabbit savedRabbit = rabbitService.create(rabbit);
        rabbitService.feedRabbit(savedRabbit.getId(), user.getId());
        rabbitService.waterRabbit(savedRabbit.getId(), user.getId());

        endOfTheDayService.process(user.getId());

        Chicken updatedChicken = chickenService.findById(savedChicken.getId());
        Rabbit updatedRabbit = rabbitService.findById(savedRabbit.getId());
        User updatedUser = userRepository.findById(user.getId()).orElseThrow();

        assertEquals(2, updatedChicken.getAge());
        assertEquals(1.65f, updatedChicken.getWeight(), 0.01f);
        assertEquals(0, updatedChicken.getFastingDays());
        assertFalse(updatedChicken.getFedToday());
        assertFalse(updatedChicken.getWateredToday());
        assertFalse(updatedChicken.getClean());

        assertEquals(11, updatedRabbit.getAge());
        assertFalse(updatedRabbit.getFedToday());
        assertFalse(updatedRabbit.getWateredToday());
        assertFalse(updatedRabbit.getClean());

        assertEquals(89F, updatedUser.getEcus(), 0.01f);
    }

    @Test
    void shouldKeepAnimalsUnchangedWhenUserIsHibernating() {
        User user = new User();
        user.setName("Sleeping Farmer");
        user.setEmail("scenario-service-2@gmail.com");
        user.setEcus(100F);
        user.setHibernation(true);
        user = userRepository.save(user);

        Chicken chicken = new Chicken();
        chicken.setUserId(user.getId());
        chicken.setName("Frozen Piou");
        chicken.setChickenType(Chicken.ChickenType.C);
        chicken.setAge(3);
        chicken.setWeight(1.4f);
        chicken.setHealthy(true);
        chicken.setClean(true);
        chicken.setFedToday(true);
        chicken.setWateredToday(true);
        chicken.setGender(Animal.AnimalGender.F);
        Chicken savedChicken = chickenService.create(chicken);

        Rabbit rabbit = new Rabbit();
        rabbit.setUserId(user.getId());
        rabbit.setName("Frozen Panpan");
        rabbit.setRabbitType(Rabbit.RabbitTypeEnum.lapereau);
        rabbit.setAge(7);
        rabbit.setWeight(2.0f);
        rabbit.setHealthy(true);
        rabbit.setClean(true);
        rabbit.setFedToday(true);
        rabbit.setWateredToday(true);
        rabbit.setGender(Animal.AnimalGender.M);
        Rabbit savedRabbit = rabbitService.create(rabbit);

        endOfTheDayService.process(user.getId());

        Chicken updatedChicken = chickenService.findById(savedChicken.getId());
        Rabbit updatedRabbit = rabbitService.findById(savedRabbit.getId());

        assertNotNull(updatedChicken);
        assertEquals(3, updatedChicken.getAge());
        assertEquals(1.4f, updatedChicken.getWeight(), 0.01f);
        assertTrue(updatedChicken.getFedToday());
        assertTrue(updatedChicken.getWateredToday());
        assertTrue(updatedChicken.getClean());

        assertNotNull(updatedRabbit);
        assertEquals(7, updatedRabbit.getAge());
        assertTrue(updatedRabbit.getFedToday());
        assertTrue(updatedRabbit.getWateredToday());
        assertTrue(updatedRabbit.getClean());
    }
}
