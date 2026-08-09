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

import platepal.model.Administrator;
import platepal.model.User;
import platepal.repository.UserRepository;
import platepal.service.UserService;

public class UserServiceTest {

    private FakeUserRepository userRepository;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = new FakeUserRepository();
        userService = new UserService(userRepository);
    }

    @Test
    @DisplayName("A registered user is stored and can be found again")
    void registerStoresTheUser() {
        User registered = userService.register("sunny", "example");

        Optional<User> stored =
                userRepository.findByUsername("sunny");

        assertTrue(stored.isPresent());
        assertEquals(registered.getId(), stored.get().getId());
        assertEquals("sunny", stored.get().getUsername());
    }

    @Test
    @DisplayName("IDs follow the U001 format and increase")
    void idsAreGeneratedInSequence() {
        assertEquals("U001", userService.register("sunny", "example").getId());
        assertEquals("U002", userService.register("nicole", "example").getId());
        assertEquals("U003", userService.register("xinpeng", "example").getId());
    }

    @Test
    @DisplayName("An ID freed up in the middle is not handed out again")
    void idsAreNotReusedAfterDeletion() {
        userService.register("sunny", "example");
        User second = userService.register("nicole", "example");
        userService.register("xinpeng", "example");

        userRepository.deleteById(second.getId());

        assertEquals("U004", userService.register("stefan", "example").getId());
    }

    @Test
    @DisplayName("Duplicate usernames are rejected")
    void duplicateUsernameIsRejected() {
        userService.register("sunny", "example");

        assertThrows(IllegalArgumentException.class,
                () -> userService.register("sunny", "different"));

        assertEquals(1, userRepository.findAll().size());
    }

    @Test
    @DisplayName("Duplicate usernames are rejected regardless of capitalisation")
    void duplicateUsernameIsRejectedIgnoringCase() {
        userService.register("sunny", "example");

        assertThrows(IllegalArgumentException.class,
                () -> userService.register("SUNNY", "example"));
    }

    @Test
    @DisplayName("A rejected registration does not consume an ID")
    void rejectedRegistrationDoesNotConsumeAnId() {
        userService.register("sunny", "example");

        assertThrows(IllegalArgumentException.class,
                () -> userService.register("sunny", "example"));

        assertEquals("U002", userService.register("nicole", "example").getId());
    }

    @Test
    @DisplayName("An empty username is rejected")
    void emptyUsernameIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.register("   ", "example"));

        assertTrue(userRepository.findAll().isEmpty());
    }

    @Test
    @DisplayName("A username containing a space is rejected")
    void usernameWithSpaceIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.register("sunny chen", "example"));
    }

    @Test
    @DisplayName("A password shorter than four characters is rejected")
    void shortPasswordIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.register("sunny", "abc"));

        assertTrue(userRepository.findAll().isEmpty());
    }

    @Test
    @DisplayName("A registered user starts with empty lists")
    void newUserStartsEmpty() {
        User user = userService.register("sunny", "example");

        assertTrue(user.getVisitedRestaurantIds().isEmpty());
        assertTrue(user.getWantToTryRestaurantIds().isEmpty());
        assertTrue(user.getFollowingUserIds().isEmpty());
        assertFalse(user.isAdministrator());
    }

    @Test
    @DisplayName("Registering an administrator produces a user with admin rights")
    void administratorIsRegisteredWithAdminRights() {
        Administrator admin =
                userService.registerAdministrator("boss", "example");

        assertTrue(admin.isAdministrator());
        assertTrue(userRepository.findByUsername("boss").isPresent());
    }

    @Test
    @DisplayName("An administrator cannot take an existing username")
    void administratorCannotTakeAnExistingUsername() {
        userService.register("sunny", "example");

        assertThrows(IllegalArgumentException.class,
                () -> userService.registerAdministrator("sunny", "example"));
    }

    @Test
    @DisplayName("Users can be looked up by ID and by username")
    void usersCanBeLookedUp() {
        User user = userService.register("sunny", "example");

        assertTrue(userService.getUserById(user.getId()).isPresent());
        assertTrue(userService.getUserByUsername("sunny").isPresent());
        assertTrue(userService.isUsernameTaken("sunny"));

        assertTrue(userService.getUserById("U999").isEmpty());
        assertFalse(userService.isUsernameTaken("nobody"));
    }

    /**
     * Keeps the tests away from the real JSON file. {@code findByUsername}
     * ignores case here exactly as {@code JsonUserRepository} does, so the
     * duplicate-username tests exercise the real behaviour.
     */
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
