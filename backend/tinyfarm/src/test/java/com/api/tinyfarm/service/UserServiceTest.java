package com.api.tinyfarm.service;

import com.api.tinyfarm.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class UserServiceTest {
    @Autowired
    UserService userService;

    @BeforeEach
    void setUp(){
        userService.deleteAllUsers();
    }

    @Test
    void shouldCreateUser() {
        User user = new User();
        user.setName("Eldoraldo");
        user.setEmail("usertest@gmail.com");
        user.setGender(User.Gender.M);

        User created = userService.create(user);

        assertNotNull(created.getId());
        assertEquals(1500, created.getEcus());
        assertEquals(1, created.getLevel());
    }

    @Test
    void shouldReturnAllUsers() {
        User user = new User();
        user.setName("Eldoraldo");
        user.setEmail("usertest@gmail.com");
        user.setGender(User.Gender.M);

        User created = userService.create(user);

        User anotherUser = new User();
        anotherUser.setName("Colorado");
        user.setEmail("usertest@gmail.com");
        anotherUser.setGender(User.Gender.F);

        User anotherUserCreated = userService.create(anotherUser);

        assertNotNull(userService.findAll());
    }

    @Test
    void shouldDeleteUser(){
        User user = new User();
        user.setName("Colorado");
        user.setEmail("usertest@gmail.com");
        user.setGender(User.Gender.F);

        User created = userService.create(user);

        userService.delete(created.getId());

        assertEquals(0, userService.findAll().size());
    }

    @Test
    void shouldHibernateUser() {
        User user = new User();
        user.setName("Colorado");
        user.setEmail("usertest@gmail.com");
        user.setGender(User.Gender.F);

        User created = userService.create(user);

        userService.hibernate(created.getId());

        User updated = userService.findById(created.getId());
        assertTrue(updated.getHibernation());
    }

}
