package platepal.controller;

import java.util.Optional;

import platepal.model.User;
import platepal.service.AuthService;
import platepal.service.UserService;

/**
 * Routes account actions from the UI to the right service.
 */
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    public AuthController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    /**
     * @throws IllegalArgumentException if the details are invalid or the
     *                                  username is already taken
     */
    public User register(String username, String password) {
        return userService.register(username, password);
    }

    /** @return the user now logged in, or empty if the credentials are wrong */
    public Optional<User> login(String username, String password) {
        return authService.login(username, password);
    }

    public void logout() {
        authService.logout();
    }

    public Optional<User> getCurrentUser() {
        return authService.getCurrentUser();
    }

    public boolean isLoggedIn() {
        return authService.isLoggedIn();
    }
}
