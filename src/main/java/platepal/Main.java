package platepal;

import java.util.Optional;
import java.util.Scanner;

import platepal.controller.AdminController;
import platepal.controller.AuthController;
import platepal.controller.PersonalListController;
import platepal.controller.RatingController;
import platepal.controller.RestaurantController;
import platepal.controller.ReviewController;
import platepal.controller.SocialController;
import platepal.model.User;
import platepal.repository.JsonRatingRepository;
import platepal.repository.JsonRestaurantRepository;
import platepal.repository.JsonReviewRepository;
import platepal.repository.JsonUserRepository;
import platepal.repository.RatingRepository;
import platepal.repository.RestaurantRepository;
import platepal.repository.ReviewRepository;
import platepal.repository.UserRepository;
import platepal.service.AdminService;
import platepal.service.AuthService;
import platepal.service.PersonalListService;
import platepal.service.PersonalRankingService;
import platepal.service.RatingService;
import platepal.service.RestaurantService;
import platepal.service.ReviewService;
import platepal.service.SocialService;
import platepal.service.UserService;
import platepal.ui.AdminMenu;
import platepal.ui.AuthMenu;
import platepal.ui.MainMenu;
import platepal.ui.PersonalListMenu;
import platepal.ui.RatingMenu;
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

        ReviewRepository reviewRepository =
                new JsonReviewRepository();

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
                        reviewRepository,
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
                new RatingService(
                        ratingRepository,
                        restaurantRepository,
                        authService);

        ReviewService reviewService =
                new ReviewService(
                        reviewRepository,
                        restaurantRepository,
                        userRepository,
                        authService);

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

        RatingController ratingController =
                new RatingController(ratingService);

        ReviewController reviewController =
                new ReviewController(reviewService);

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
                            reviewController,
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

            RatingMenu ratingMenu =
                    new RatingMenu(
                            ratingController,
                            reviewController,
                            restaurantController,
                            scanner);

            MainMenu mainMenu =
                    new MainMenu(
                            authController,
                            restaurantMenu,
                            personalListMenu,
                            ratingMenu,
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