package platepal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import platepal.model.Administrator;
import platepal.model.User;

class UserTest {

    @Test
    @DisplayName("Marking a restaurant visited removes it from Want to Try")
    void visitedRemovesFromWantToTry() {
        User user = new User("U001", "stefan", "example");
        user.addToWantToTry("R001");
        user.markAsVisited("R001");

        assertTrue(user.hasVisited("R001"));
        assertFalse(user.wantsToTry("R001"));
    }

    @Test
    @DisplayName("An already visited restaurant is not added back to Want to Try")
    void visitedRestaurantCannotReturnToWantToTry() {
        User user = new User("U001", "stefan", "example");
        user.markAsVisited("R001");

        assertFalse(user.addToWantToTry("R001"));
        assertFalse(user.wantsToTry("R001"));
    }

    @Test
    @DisplayName("A user cannot follow themselves")
    void cannotFollowSelf() {
        User user = new User("U001", "stefan", "example");
        assertThrows(IllegalArgumentException.class, () -> user.follow("U001"));
    }

    @Test
    @DisplayName("Regular users are not administrators")
    void regularUserHasNoAdminRights() {
        assertFalse(new User("U001", "stefan", "example").isAdministrator());
    }

    @Test
    @DisplayName("Administrators are users with management rights")
    void administratorHasAdminRights() {
        Administrator admin = new Administrator("U000", "admin", "admin123");
        assertTrue(admin.isAdministrator());
        assertTrue(admin instanceof User);
    }

    @Test
    @DisplayName("The returned lists cannot be modified from outside")
    void listsAreUnmodifiable() {
        User user = new User("U001", "stefan", "example");
        assertThrows(UnsupportedOperationException.class,
                () -> user.getVisitedRestaurantIds().add("R999"));
    }
}
