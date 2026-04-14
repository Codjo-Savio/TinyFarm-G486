package com.api.tinyfarm.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.api.tinyfarm.model.User;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
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
        user.setEmail("usertest@gmail.com");
        user.setGender(User.Gender.M);
        user.setEcus(1500F);
        user.setLevel(1);

        // ACT
        User saved = userRepository.save(user);

        // ASSERT
        assertNotNull(saved.getId()); // PK = u_id
        assertEquals("Marcel", saved.getName());
        assertEquals(1500F, saved.getEcus());
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
        u1.setEmail("usertest@gmail.com");
        u1.setGender(User.Gender.M);

        User u2 = new User();
        u2.setName("Huguette");
        u2.setEmail("usertest2@gmail.com");
        u2.setGender(User.Gender.F);

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
        user.setName("Marcel");
        user.setEcus(1500F);
        userRepository.save(user);

        // ACT — vend 1 oeuf à 8 écus
        user.setEcus(1508F);
        User updated = userRepository.save(user);

        // ASSERT
        assertEquals(1508F, updated.getEcus());
    }

    @Test
    void shouldDeleteUser() {
        // ARRANGE
        User user = new User();
        user.setName("Marcel");
        userRepository.save(user);

        // ACT
        userRepository.deleteById(user.getId());

        // ASSERT
        Optional<User> found = userRepository.findById(user.getId());
        assertFalse(found.isPresent());
    }
}
