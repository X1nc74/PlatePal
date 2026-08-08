package platepal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import platepal.model.User;
import platepal.repository.UserRepository;
import platepal.service.AuthService;
import platepal.service.UserService;

public class AuthServiceTest {

    private AuthService authService;

    @BeforeEach
    void setUp() {
        UserRepository userRepository = new FakeUserRepository();
        UserService userService = new UserService(userRepository);

        userService.register("sunny", "example");
        userService.registerAdministrator("boss", "adminpass");

        authService = new AuthService(userRepository);
    }

    @Test
    @DisplayName("Correct credentials log the user in")
    void correctCredentialsLogIn() {
        Optional<User> loggedIn = authService.login("sunny", "example");

        assertTrue(loggedIn.isPresent());
        assertEquals("sunny", loggedIn.get().getUsername());
        assertTrue(authService.isLoggedIn());
        assertEquals("U001", authService.requireCurrentUser().getId());
    }

    @Test
    @DisplayName("An incorrect password is rejected and starts no session")
    void incorrectPasswordIsRejected() {
        assertTrue(authService.login("sunny", "wrong").isEmpty());
        assertFalse(authService.isLoggedIn());
    }

    @Test
    @DisplayName("An unknown username is rejected")
    void unknownUsernameIsRejected() {
        assertTrue(authService.login("nobody", "example").isEmpty());
        assertFalse(authService.isLoggedIn());
    }

    @Test
    @DisplayName("Logging in works regardless of how the username is capitalised")
    void loginIgnoresUsernameCase() {
        assertTrue(authService.login("SUNNY", "example").isPresent());
    }

    @Test
    @DisplayName("The password must match exactly, including case")
    void passwordIsCaseSensitive() {
        assertTrue(authService.login("sunny", "EXAMPLE").isEmpty());
    }

    @Test
    @DisplayName("Null credentials are rejected without throwing")
    void nullCredentialsAreRejected() {
        assertTrue(authService.login(null, "example").isEmpty());
        assertTrue(authService.login("sunny", null).isEmpty());
        assertFalse(authService.isLoggedIn());
    }

    @Test
    @DisplayName("A failed login does not end an existing session")
    void failedLoginKeepsExistingSession() {
        authService.login("sunny", "example");
        authService.login("sunny", "wrong");

        assertTrue(authService.isLoggedIn());
        assertEquals("sunny", authService.requireCurrentUser().getUsername());
    }

    @Test
    @DisplayName("Logging out ends the session")
    void logoutEndsTheSession() {
        authService.login("sunny", "example");
        authService.logout();

        assertFalse(authService.isLoggedIn());
        assertTrue(authService.getCurrentUser().isEmpty());
    }

    @Test
    @DisplayName("Asking for the current user while logged out is a programming error")
    void requireCurrentUserFailsWhenLoggedOut() {
        assertThrows(IllegalStateException.class,
                () -> authService.requireCurrentUser());
    }

    @Test
    @DisplayName("Only an administrator account reports administrator rights")
    void administratorRightsFollowTheAccount() {
        assertFalse(authService.isCurrentUserAdministrator());

        authService.login("sunny", "example");
        assertFalse(authService.isCurrentUserAdministrator());

        authService.login("boss", "adminpass");
        assertTrue(authService.isCurrentUserAdministrator());
    }

    /** Keeps the tests away from the real JSON file. */
    private static class FakeUserRepository implements UserRepository {

        private final List<User> users = new ArrayList<>();

        @Override
        public List<User> findAll() {
            return new ArrayList<>(users);
        }

        @Override
        public Optional<User> findById(String id) {
            return users.stream()
                    .filter(u -> u.getId().equals(id))
                    .findFirst();
        }

        @Override
        public Optional<User> findByUsername(String username) {
            if (username == null || username.isBlank()) {
                return Optional.empty();
            }

            String wanted = username.trim();

            return users.stream()
                    .filter(u -> u.getUsername().equalsIgnoreCase(wanted))
                    .findFirst();
        }

        @Override
        public void save(User user) {
            users.removeIf(u -> u.getId().equals(user.getId()));
            users.add(user);
        }

        @Override
        public void deleteById(String id) {
            users.removeIf(u -> u.getId().equals(id));
        }
    }
}
