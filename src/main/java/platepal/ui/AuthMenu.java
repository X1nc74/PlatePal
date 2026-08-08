package platepal.ui;

import java.util.Optional;
import java.util.Scanner;

import platepal.controller.AuthController;
import platepal.model.User;

/**
 * The register-or-log-in screen shown before the rest of the application.
 */
public class AuthMenu {

    private final AuthController controller;
    private final Scanner scanner;

    public AuthMenu(AuthController controller, Scanner scanner) {
        this.controller = controller;
        this.scanner = scanner;
    }

    /**
     * Runs the screen until somebody logs in or chooses to leave.
     *
     * @return the user who logged in, or empty if the program should close
     */
    public Optional<User> show() {
        while (true) {
            printMenu();

            String choice = readLine();

            if (choice == null) {
                return Optional.empty();
            }

            switch (choice) {
                case "1":
                    register();
                    break;

                case "2":
                    login();
                    break;

                case "0":
                    return Optional.empty();

                default:
                    System.out.println("Invalid option.");
            }

            if (controller.isLoggedIn()) {
                return controller.getCurrentUser();
            }
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println("=== Welcome to PlatePal ===");
        System.out.println("1. Register");
        System.out.println("2. Log in");
        System.out.println("0. Exit");
        System.out.print("Choose an option: ");
    }

    /**
     * Registration deliberately does not start a session. The user registers,
     * then logs in, which is the workflow in section 16 of the alignment
     * document and keeps the two features demonstrable on their own.
     */
    private void register() {
        System.out.print("Choose a username: ");
        String username = readLine();

        System.out.print("Choose a password: ");
        String password = readLine();

        if (username == null || password == null) {
            return;
        }

        try {
            User created = controller.register(username, password);

            System.out.println(
                    "Account created for " + created.getUsername()
                            + " (" + created.getId() + "). You can now log in.");

        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private void login() {
        System.out.print("Username: ");
        String username = readLine();

        System.out.print("Password: ");
        String password = readLine();

        if (username == null || password == null) {
            return;
        }

        controller.login(username, password)
                .ifPresentOrElse(
                        user -> System.out.println(
                                "Welcome back, " + user.getUsername() + "!"),
                        () -> System.out.println(
                                "Invalid username or password."));
    }

    /**
     * @return the next line, or null when the input has run out, so that piped
     *         input and a closed terminal end the program instead of throwing
     */
    private String readLine() {
        if (!scanner.hasNextLine()) {
            return null;
        }

        return scanner.nextLine().trim();
    }
}
