package com.api.tinyfarm.service;

import com.api.tinyfarm.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class UserServiceTest {
    @Autowired
    UserService userService;

    @BeforeEach
    void setUp() {
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
    void shouldDeleteUser() {
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

    @Test
    void shouldDeleteExpiredHibernations() {
        User expiredUser = new User();
        expiredUser.setName("Expired");
        expiredUser.setEmail("expired@gmail.com");
        expiredUser.setGender(User.Gender.M);
        expiredUser.setHibernation(true);
        expiredUser.setHibernationDate(LocalDateTime.now().minusDays(51));
        userService.create(expiredUser);

        User recentUser = new User();
        recentUser.setName("Recent");
        recentUser.setEmail("recent@gmail.com");
        recentUser.setGender(User.Gender.F);
        recentUser.setHibernation(true);
        recentUser.setHibernationDate(LocalDateTime.now().minusDays(10));
        userService.create(recentUser);

        User activeUser = new User();
        activeUser.setName("Active");
        activeUser.setEmail("active@gmail.com");
        activeUser.setGender(User.Gender.F);
        userService.create(activeUser);

        userService.deleteExpiredHibernations();

        assertEquals(2, userService.findAll().size());
        assertThrows(RuntimeException.class, () -> userService.findByEmail("expired@gmail.com"));
        assertNotNull(userService.findByEmail("recent@gmail.com"));
        assertNotNull(userService.findByEmail("active@gmail.com"));
    }

    @Test
    void shouldFindOrCreateUser() {
        User created = userService.findOrCreateOAuthUser(
                "oauth@gmail.com",
                "OAuth User",
                User.Gender.M
        );

        assertNotNull(created.getId());
        assertEquals("OAuth User", created.getName());
        assertEquals("oauth@gmail.com", created.getEmail());
        assertEquals(User.Gender.M, created.getGender());
        assertFalse(created.getHibernation());

        userService.hibernate(created.getId());

        User found = userService.findOrCreateOAuthUser(
                "oauth@gmail.com",
                "Updated OAuth User",
                User.Gender.F
        );

        assertEquals(created.getId(), found.getId());
        assertEquals("Updated OAuth User", found.getName());
        assertEquals(User.Gender.F, found.getGender());
        assertFalse(found.getHibernation());
        assertNull(found.getHibernationDate());
        assertEquals(1, userService.findAll().size());
    }

    @Test
    void shouldResetRemainingPurchasesAtMidnightJob() {
        User userOne = new User();
        userOne.setName("Reset One");
        userOne.setEmail("reset.one@gmail.com");
        userOne.setGender(User.Gender.M);
        userOne.setRemainingPurchases(2);
        userService.create(userOne);

        User userTwo = new User();
        userTwo.setName("Reset Two");
        userTwo.setEmail("reset.two@gmail.com");
        userTwo.setGender(User.Gender.F);
        userTwo.setRemainingPurchases(0);
        userService.create(userTwo);

        userService.resetRemainingPurchases();

        assertEquals(12, userService.findByEmail("reset.one@gmail.com").getRemainingPurchases());
        assertEquals(12, userService.findByEmail("reset.two@gmail.com").getRemainingPurchases());
    }
}
