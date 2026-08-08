package platepal.ui;

import java.util.Scanner;

import platepal.controller.AuthController;
import platepal.model.User;

/**
 * The screen shown after logging in, routing to the other menus.
 */
public class MainMenu {

    private final AuthController authController;
    private final RestaurantMenu restaurantMenu;
    private final PersonalListMenu personalListMenu;
    private final SocialMenu socialMenu;
    private final AdminMenu adminMenu;
    private final Scanner scanner;

    public MainMenu(AuthController authController,
                    RestaurantMenu restaurantMenu,
                    PersonalListMenu personalListMenu,
                    SocialMenu socialMenu,
                    AdminMenu adminMenu,
                    Scanner scanner) {

        this.authController = authController;
        this.restaurantMenu = restaurantMenu;
        this.personalListMenu = personalListMenu;
        this.socialMenu = socialMenu;
        this.adminMenu = adminMenu;
        this.scanner = scanner;
    }

    public void show() {
        boolean running = true;

        while (running) {
            printMenu();

            String choice = readLine();

            if (choice == null) {
                return;
            }

            switch (choice) {
                case "1":
                    restaurantMenu.show();
                    break;

                case "2":
                    personalListMenu.show();
                    break;

                case "3":
                    socialMenu.show();
                    break;

                case "4":
                    // Hiding the option is not enough on its own: someone can
                    // still type 4. AdminService checks the permission again, so
                    // this only decides what the screen does, not what is allowed.
                    if (currentUserIsAdministrator()) {
                        adminMenu.show();
                    } else {
                        System.out.println("Invalid option.");
                    }
                    break;

                case "0":
                    running = false;
                    break;

                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println("=== PlatePal ===");
        System.out.println("1. Restaurant discovery");
        System.out.println("2. My lists");
        System.out.println("3. Users");

        if (currentUserIsAdministrator()) {
            System.out.println("4. Manage restaurants (administrator)");
        }

        System.out.println("0. Log out");
        System.out.print("Choose an option: ");
    }

    private boolean currentUserIsAdministrator() {
        return authController.getCurrentUser()
                .map(User::isAdministrator)
                .orElse(false);
    }

    /** @return the next line, or null when the input has run out */
    private String readLine() {
        if (!scanner.hasNextLine()) {
            return null;
        }

        return scanner.nextLine().trim();
    }
}
