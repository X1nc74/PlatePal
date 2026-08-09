package platepal.service;

import java.util.Optional;

import platepal.model.User;
import platepal.repository.UserRepository;

/**
 * Verifies credentials and remembers who is currently logged in.
 */
public class AuthService {

    private final UserRepository userRepository;

    private User currentUser;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Checks a username and password and, when they match, starts a session.
     *
     * <p>An empty result means "those credentials are wrong". Nothing is thrown,
     * because a mistyped password is an ordinary thing for a user to do, not an
     * exceptional condition. The caller cannot tell whether the username or the
     * password was wrong, which is deliberate: saying which one exists would let
     * anyone discover who has an account.
     *
     * @return the user now logged in, or empty if the credentials do not match
     */
    public Optional<User> login(String username, String password) {
        if (username == null || password == null) {
            return Optional.empty();
        }

        Optional<User> match = userRepository.findByUsername(username)
                .filter(user -> user.matchesPassword(password));

        match.ifPresent(user -> this.currentUser = user);

        return match;
    }

    public void logout() {
        this.currentUser = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * @return the logged-in user, or empty if nobody is logged in
     */
    public Optional<User> getCurrentUser() {
        return Optional.ofNullable(currentUser);
    }

    /**
     * The same as {@link #getCurrentUser()} for code that only runs behind the
     * login screen and would have no sensible way to continue without a user.
     *
     * @throws IllegalStateException if nobody is logged in
     */
    public User requireCurrentUser() {
        if (currentUser == null) {
            throw new IllegalStateException("No user is logged in.");
        }

        return currentUser;
    }

    /** Permission check for the administrator-only features. */
    public boolean isCurrentUserAdministrator() {
        return currentUser != null && currentUser.isAdministrator();
    }

    /**
     * Re-reads the logged-in user from storage.
     *
     * <p>The session holds the object that was loaded at login. Saving a change
     * through another service writes a new copy to the file, so anything that
     * updates the current user should call this afterwards to avoid working from
     * a stale copy.
     */
    public void refreshCurrentUser() {
        if (currentUser != null) {
            userRepository.findById(currentUser.getId())
                    .ifPresent(user -> this.currentUser = user);
        }
    }
}
