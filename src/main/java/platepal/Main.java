package platepal;

import java.util.Optional;
import java.util.Scanner;

import platepal.controller.AdminController;
import platepal.controller.AuthController;
import platepal.controller.PersonalListController;
import platepal.controller.RestaurantController;
import platepal.controller.SocialController;
import platepal.model.User;
import platepal.repository.JsonRatingRepository;
import platepal.repository.JsonRestaurantRepository;
import platepal.repository.JsonUserRepository;
import platepal.repository.RatingRepository;
import platepal.repository.RestaurantRepository;
import platepal.repository.UserRepository;
import platepal.service.AdminService;
import platepal.service.AuthService;
import platepal.service.PersonalListService;
import platepal.service.PersonalRankingService;
import platepal.service.RatingService;
import platepal.service.RestaurantService;
import platepal.service.SocialService;
import platepal.service.UserService;
import platepal.ui.AdminMenu;
import platepal.ui.AuthMenu;
import platepal.ui.MainMenu;
import platepal.ui.PersonalListMenu;
import platepal.ui.RestaurantMenu;
import platepal.ui.SocialMenu;

public class Main {

    public static void main(String[] args) {

        UserRepository userRepository =
                new JsonUserRepository();

        RestaurantRepository restaurantRepository =
                new JsonRestaurantRepository();

        RatingRepository ratingRepository =
                new JsonRatingRepository();

        UserService userService =
                new UserService(userRepository);

        AuthService authService =
                new AuthService(userRepository);

        RestaurantService restaurantService =
                new RestaurantService(restaurantRepository);

        AdminService adminService =
                new AdminService(
                        restaurantRepository,
                        ratingRepository,
                        userRepository,
                        authService);

        PersonalListService personalListService =
                new PersonalListService(
                        userRepository,
                        restaurantRepository,
                        authService);

        SocialService socialService =
                new SocialService(
                        userRepository,
                        restaurantRepository,
                        authService);

        RatingService ratingService =
                new RatingService(ratingRepository);

        PersonalRankingService personalRankingService =
                new PersonalRankingService(
                        ratingService,
                        restaurantRepository);

        AuthController authController =
                new AuthController(
                        userService,
                        authService);

        AdminController adminController =
                new AdminController(
                        adminService,
                        restaurantService);

        PersonalListController personalListController =
                new PersonalListController(personalListService);

        SocialController socialController =
                new SocialController(socialService);

        RestaurantController restaurantController =
                new RestaurantController(
                        restaurantService,
                        personalRankingService);

        userService.ensureAdministratorExists("admin", "admin123")
                .ifPresent(admin -> System.out.println(
                        "No administrator existed, so one was created: "
                                + "username 'admin', password 'admin123'."));

        try (Scanner scanner = new Scanner(System.in)) {

            AuthMenu authMenu =
                    new AuthMenu(
                            authController,
                            scanner);

            RestaurantMenu restaurantMenu =
                    new RestaurantMenu(
                            restaurantController,
                            ratingService,
                            scanner);

            AdminMenu adminMenu =
                    new AdminMenu(
                            adminController,
                            scanner);

            PersonalListMenu personalListMenu =
                    new PersonalListMenu(
                            personalListController,
                            scanner);

            SocialMenu socialMenu =
                    new SocialMenu(
                            socialController,
                            scanner);

            MainMenu mainMenu =
                    new MainMenu(
                            authController,
                            restaurantMenu,
                            personalListMenu,
                            socialMenu,
                            adminMenu,
                            scanner);

            Optional<User> loggedIn = authMenu.show();

            if (loggedIn.isEmpty()) {
                System.out.println("Goodbye!");
                return;
            }

            mainMenu.show();

            authController.logout();

            System.out.println(
                    "Goodbye, " + loggedIn.get().getUsername() + "!");
        }
    }
}