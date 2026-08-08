package platepal;

import java.util.Optional;
import java.util.Scanner;

import platepal.controller.AuthController;
import platepal.controller.RestaurantController;
import platepal.model.User;
import platepal.repository.JsonRatingRepository;
import platepal.repository.JsonRestaurantRepository;
import platepal.repository.JsonUserRepository;
import platepal.repository.RatingRepository;
import platepal.repository.RestaurantRepository;
import platepal.repository.UserRepository;
import platepal.service.AuthService;
import platepal.service.PersonalRankingService;
import platepal.service.RatingService;
import platepal.service.RestaurantService;
import platepal.service.UserService;
import platepal.ui.AuthMenu;
import platepal.ui.RestaurantMenu;

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

        RestaurantController restaurantController =
                new RestaurantController(
                        restaurantService,
                        personalRankingService);

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

            Optional<User> loggedIn = authMenu.show();

            if (loggedIn.isEmpty()) {
                System.out.println("Goodbye!");
                return;
            }

            restaurantMenu.show();

            authController.logout();

            System.out.println(
                    "Goodbye, " + loggedIn.get().getUsername() + "!");
        }
    }
}