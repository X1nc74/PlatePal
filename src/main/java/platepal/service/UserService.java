package platepal.service;

import java.util.List;
import java.util.Optional;

import platepal.model.Administrator;
import platepal.model.User;
import platepal.repository.UserRepository;

/**
 * Account creation and user lookups.
 */
public class UserService {

    private static final String ID_PREFIX = "U";

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Creates a regular account and stores it.
     *
     * <p>The {@link User} constructor validates the username and password, so
     * those rules stay in one place. Uniqueness is checked here instead, because
     * a single User object cannot see the other users.
     *
     * @throws IllegalArgumentException if the details are invalid or the
     *                                  username is already taken
     */
    public User register(String username, String password) {
        User user = new User(nextUserId(), username, password);

        requireUsernameAvailable(user.getUsername());
        userRepository.save(user);

        return user;
    }

    /**
     * Creates an administrator account. Same rules as {@link #register}; only the
     * stored role and the resulting permissions differ.
     */
    public Administrator registerAdministrator(String username, String password) {
        Administrator administrator =
                new Administrator(nextUserId(), username, password);

        requireUsernameAvailable(administrator.getUsername());
        userRepository.save(administrator);

        return administrator;
    }

    /**
     * Creates a starting administrator account when the data file has none.
     *
     * <p>Administrator accounts can only be created by this method or by
     * {@link #registerAdministrator}, and the register screen deliberately does
     * not offer the option, so a fresh copy of the project would otherwise have
     * no way to reach the restaurant management features at all.
     *
     * @return the account that was created, or empty if an administrator already
     *         existed or the username was taken by somebody else
     */
    public Optional<Administrator> ensureAdministratorExists(String username,
                                                             String password) {
        boolean administratorExists = userRepository.findAll()
                .stream()
                .anyMatch(User::isAdministrator);

        if (administratorExists || isUsernameTaken(username)) {
            return Optional.empty();
        }

        return Optional.of(registerAdministrator(username, password));
    }

    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public boolean isUsernameTaken(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    private void requireUsernameAvailable(String username) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException(
                    "Username '" + username + "' is already taken.");
        }
    }

    /**
     * Produces the next ID in the {@code U001} format 
     *
     * <p>The next number comes from the highest existing ID rather than from the
     * number of users, so an ID left behind in the middle by a deleted account
     * is not handed out again. Ratings and Want to Try lists refer to users by
     * ID, and a reused ID would silently attach one person's data to another.
     *
     * <p>Deleting the newest account is the one case this does not cover: its ID
     * becomes the highest again and would be reissued. 
     */
    private String nextUserId() {
        int highest = 0;

        for (User user : userRepository.findAll()) {
            highest = Math.max(highest, numericSuffix(user.getId()));
        }

        return String.format("%s%03d", ID_PREFIX, highest + 1);
    }

    /** @return the number in an ID such as {@code U007}, or 0 if unreadable. */
    private static int numericSuffix(String id) {
        if (id == null || !id.startsWith(ID_PREFIX)) {
            return 0;
        }

        try {
            return Integer.parseInt(id.substring(ID_PREFIX.length()));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
