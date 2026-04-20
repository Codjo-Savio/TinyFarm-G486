package com.api.tinyfarm.controller;

import com.api.tinyfarm.model.Animal;
import com.api.tinyfarm.model.Chicken;
import com.api.tinyfarm.model.Rabbit;
import com.api.tinyfarm.model.User;
import com.api.tinyfarm.repository.UserRepository;
import com.api.tinyfarm.service.AnimalService;
import com.api.tinyfarm.service.ChickenService;
import com.api.tinyfarm.service.RabbitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class EndOfDayControllerScenarioTest extends AuthenticatedControllerTestSupport {

    @Autowired
    private MockMvc mockMvc;

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
    void shouldProcessEndOfDayFromHttpEndpointAndApplyExpectedValues() throws Exception {
        User user = new User();
        user.setName("Controller Farmer");
        user.setEmail("scenario-controller-1@gmail.com");
        user.setEcus(100F);
        user = userRepository.save(user);

        Chicken chicken = new Chicken();
        chicken.setUserId(user.getId());
        chicken.setName("Http Piou");
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
        rabbit.setName("Http Panpan");
        rabbit.setRabbitType(Rabbit.RabbitTypeEnum.lapereau);
        rabbit.setAge(10);
        rabbit.setWeight(2.5f);
        rabbit.setHealthy(true);
        rabbit.setClean(true);
        rabbit.setGender(Animal.AnimalGender.M);
        Rabbit savedRabbit = rabbitService.create(rabbit);
        rabbitService.feedRabbit(savedRabbit.getId(), user.getId());

        mockMvc.perform(post("/api/endofday/id/" + user.getId()).with(authenticated()))
                .andExpect(status().isOk());

        Chicken updatedChicken = chickenService.findById(savedChicken.getId());
        Rabbit updatedRabbit = rabbitService.findById(savedRabbit.getId());

        assertEquals(2, updatedChicken.getAge());
        assertEquals(1.65f, updatedChicken.getWeight(), 0.01f);
        assertEquals(10, updatedRabbit.getAge());
        assertFalse(updatedRabbit.getClean());
    }

    @Test
    void shouldReturnNotFoundForUnknownUserId() throws Exception {
        mockMvc.perform(post("/api/endofday/id/999999").with(authenticated()))
                .andExpect(status().isNotFound());

        assertEquals(0, chickenService.findAll().size());
        assertEquals(0, rabbitService.findAll().size());
        assertFalse(userRepository.findById(999999L).isPresent());
    }
}
