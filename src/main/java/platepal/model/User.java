package platepal.model;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A regular PlatePal user.
 *
 * <p>Relationships to other domain objects are stored as IDs, not as object
 * references. 
 *
 * <p>Business rules enforced here (structural invariants only):
 * <ul>
 *   <li>A restaurant cannot be in the Visited list and the Want to Try list at
 *       the same time.</li>
 *   <li>Marking a restaurant as visited removes it from Want to Try.</li>
 *   <li>A user cannot follow themselves.</li>
 * </ul>
 * Rules that require looking at other objects (for example "only visited
 * restaurants may be rated") belong in the service layer.
 */
public class User {

    private String id;
    private String username;
    private String password;
    private Role role;

    private Set<String> visitedRestaurantIds = new LinkedHashSet<>();
    private Set<String> wantToTryRestaurantIds = new LinkedHashSet<>();
    private Set<String> followingUserIds = new LinkedHashSet<>();

    /** Required by Gson. Do not use directly in application code. */
    protected User() {
    }

    public User(String id, String username, String password) {
        this(id, username, password, Role.USER);
    }

    protected User(String id, String username, String password, Role role) {
        setId(id);
        setUsername(username);
        setPassword(password);
        this.role = Objects.requireNonNull(role, "Role must not be null.");
    }

    // ---------------------------------------------------------------- identity

    public String getId() {
        return id;
    }

    private void setId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("User id must not be empty.");
        }
        this.id = id.trim();
    }

    public String getUsername() {
        return username;
    }

    /**
     * Usernames must be non-empty and contain no whitespace, so that they can be
     * typed as a single CLI argument. Uniqueness is checked in UserService,
     * because a single User object cannot see the other users.
     */
    public void setUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username must not be empty.");
        }
        String trimmed = username.trim();
        if (trimmed.contains(" ")) {
            throw new IllegalArgumentException("Username must not contain spaces.");
        }
        this.username = trimmed;
    }

    /**
     * Package-private on purpose: nothing outside the model should read a
     * password back out. Login is verified with {@link #matchesPassword(String)}.
     */
    String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if (password == null || password.length() < 4) {
            throw new IllegalArgumentException("Password must be at least 4 characters long.");
        }
        this.password = password;
    }

    public boolean matchesPassword(String candidate) {
        return password != null && password.equals(candidate);
    }

    public Role getRole() {
        return role;
    }

    /**
     * Used by the service layer for permission checks. Overridden in
     * {@link Administrator}.
     */
    public boolean isAdministrator() {
        return false;
    }

    // ------------------------------------------------------------ visited list

    /** @return an unmodifiable view; use the mutator methods to change the list. */
    public Set<String> getVisitedRestaurantIds() {
        return Collections.unmodifiableSet(visitedRestaurantIds);
    }

    /**
     * Marks a restaurant as visited. Business rules 3 and 4: the restaurant is
     * removed from Want to Try so that it never appears in both lists.
     *
     * @return true if the restaurant was not already in the Visited list
     */
    public boolean markAsVisited(String restaurantId) {
        requireRestaurantId(restaurantId);
        wantToTryRestaurantIds.remove(restaurantId);
        return visitedRestaurantIds.add(restaurantId);
    }

    public boolean hasVisited(String restaurantId) {
        return visitedRestaurantIds.contains(restaurantId);
    }

    public boolean removeFromVisited(String restaurantId) {
        return visitedRestaurantIds.remove(restaurantId);
    }

    // -------------------------------------------------------- want to try list

    /** @return an unmodifiable view; use the mutator methods to change the list. */
    public Set<String> getWantToTryRestaurantIds() {
        return Collections.unmodifiableSet(wantToTryRestaurantIds);
    }

    /**
     * Adds a restaurant to Want to Try. A restaurant that has already been
     * visited is not added back, which keeps the two lists disjoint.
     *
     * @return true if the restaurant was added
     */
    public boolean addToWantToTry(String restaurantId) {
        requireRestaurantId(restaurantId);
        if (visitedRestaurantIds.contains(restaurantId)) {
            return false;
        }
        return wantToTryRestaurantIds.add(restaurantId);
    }

    public boolean wantsToTry(String restaurantId) {
        return wantToTryRestaurantIds.contains(restaurantId);
    }

    public boolean removeFromWantToTry(String restaurantId) {
        return wantToTryRestaurantIds.remove(restaurantId);
    }

    /**
     * Called when an administrator deletes a restaurant, so that no list keeps a
     * dangling ID (final testing checklist, phase 8).
     */
    public void forgetRestaurant(String restaurantId) {
        visitedRestaurantIds.remove(restaurantId);
        wantToTryRestaurantIds.remove(restaurantId);
    }

    // ------------------------------------------------------------------ social

    /** @return an unmodifiable view of the users this user follows. */
    public Set<String> getFollowingUserIds() {
        return Collections.unmodifiableSet(followingUserIds);
    }

    /**
     * One-way follow: the other user does not approve the relationship.
     *
     * @return true if this user was not already following that user
     * @throws IllegalArgumentException if a user tries to follow themselves
     */
    public boolean follow(String otherUserId) {
        if (otherUserId == null || otherUserId.isBlank()) {
            throw new IllegalArgumentException("User id must not be empty.");
        }
        if (otherUserId.equals(this.id)) {
            throw new IllegalArgumentException("A user cannot follow themselves.");
        }
        return followingUserIds.add(otherUserId);
    }

    public boolean unfollow(String otherUserId) {
        return followingUserIds.remove(otherUserId);
    }

    public boolean isFollowing(String otherUserId) {
        return followingUserIds.contains(otherUserId);
    }

    // ----------------------------------------------------------------- helpers

    private static void requireRestaurantId(String restaurantId) {
        if (restaurantId == null || restaurantId.isBlank()) {
            throw new IllegalArgumentException("Restaurant id must not be empty.");
        }
    }

    /**
     * Gson bypasses constructors, so collections declared with an initializer can
     * still come back null if the field was absent from the JSON file. The
     * persistence layer calls this after loading.
     */
    public void repairAfterDeserialization() {
        if (visitedRestaurantIds == null) {
            visitedRestaurantIds = new LinkedHashSet<>();
        }
        if (wantToTryRestaurantIds == null) {
            wantToTryRestaurantIds = new LinkedHashSet<>();
        }
        if (followingUserIds == null) {
            followingUserIds = new LinkedHashSet<>();
        }
        if (role == null) {
            role = Role.USER;
        }
        // Defensive: keep the two lists disjoint even if the file was hand-edited.
        wantToTryRestaurantIds.removeAll(visitedRestaurantIds);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof User)) {
            return false;
        }
        return Objects.equals(id, ((User) other).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return username + " (" + id + ", " + role + ")";
    }
}
