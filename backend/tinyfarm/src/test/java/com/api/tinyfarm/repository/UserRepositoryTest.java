package com.api.tinyfarm.repository;

import com.api.tinyfarm.model.User;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Optional;
import java.util.List;

@DataJpaTest
@ActiveProfiles("test")
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void shouldSaveUser() {
        // ARRANGE
        User user = new User();
        user.setName("Marcel");
        user.setGender("M");
        user.setEcus(1500);
        user.setLevel(1);

        // ACT
        User saved = userRepository.save(user);

        // ASSERT
        assertNotNull(saved.getId()); // PK = u_id
        assertEquals("Marcel", saved.getName());
        assertEquals(1500, saved.getEcus());
        assertEquals(1, saved.getLevel());
    }

    @Test
    void shouldFindUserById() {
        // ARRANGE
        User user = new User();
        user.setName("Huguette");
        userRepository.save(user);

        // ACT
        Optional<User> found = userRepository.findById(user.getId());

        // ASSERT
        assertTrue(found.isPresent());
        assertEquals("Huguette", found.get().getName());
    }

    @Test
    void shouldFindAllUsers() {
        // ARRANGE
        User u1 = new User();
        u1.setName("Marcel");

        User u2 = new User();
        u2.setName("Huguette");

        userRepository.save(u1);
        userRepository.save(u2);

        // ACT
        List<User> users = userRepository.findAll();

        // ASSERT
        assertEquals(2, users.size());
    }

    @Test
    void shouldUpdateUserEcus() {
        // ARRANGE
        User user = new User();
        user.setEcus(1500);
        userRepository.save(user);

        // ACT — vend 1 oeuf à 8 écus
        user.setEcus(1508);
        User updated = userRepository.save(user);

        // ASSERT
        assertEquals(1508, updated.getEcus());
    }

    @Test
    void shouldDeleteUser() {
        // ARRANGE
        User user = new User();
        userRepository.save(user);

        // ACT
        userRepository.deleteById(user.getId());

        // ASSERT
        Optional<User> found = userRepository.findById(user.getId());
        assertFalse(found.isPresent());
    }
}