package com.bookmarks.data;

import com.bookmarks.User;
import com.bookmarks.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.context.annotation.Import;
import com.bookmarks.config.AppConfig;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(AppConfig.class)
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    public void testUserRegistrationAndSecurePersistence() {
        // Arrange
        String username = "alice";
        String email = "alice@example.com";
        String rawPassword = "securePassword123";
        String hashedPassword = passwordEncoder.encode(rawPassword);

        User newUser = new User(username, email, hashedPassword);

        // Act
        User savedUser = userRepository.save(newUser);

        // Assert
        assertNotNull(savedUser.getId());
        assertEquals(username, savedUser.getUsername());
        assertEquals(email, savedUser.getEmail());
        assertNotNull(savedUser.getCreatedAt());
        assertNotNull(savedUser.getUpdatedAt());

        // Verify that the password is not stored in plaintext and is a valid BCrypt hash
        assertNotEquals(rawPassword, savedUser.getPasswordHash());
        assertTrue(passwordEncoder.matches(rawPassword, savedUser.getPasswordHash()));
    }

    @Test
    public void testLoginAttemptAndCredentialValidation() {
        // Arrange
        String username = "bob";
        String email = "bob@example.com";
        String rawPassword = "mySecretPassword";
        String hashedPassword = passwordEncoder.encode(rawPassword);

        User newUser = new User(username, email, hashedPassword);
        userRepository.save(newUser);

        // Act - Query the user
        Optional<User> queriedUserOpt = userRepository.findByUsername(username);

        // Assert - Correct credentials validate successfully
        assertTrue(queriedUserOpt.isPresent());
        User retrievedUser = queriedUserOpt.get();
        assertTrue(passwordEncoder.matches(rawPassword, retrievedUser.getPasswordHash()));

        // Assert - Incorrect credentials fail validation
        assertFalse(passwordEncoder.matches("wrongPassword", retrievedUser.getPasswordHash()));
    }
}
