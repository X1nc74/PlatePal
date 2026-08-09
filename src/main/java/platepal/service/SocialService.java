package platepal.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import platepal.model.Rating;
import platepal.model.Restaurant;
import platepal.model.User;
import platepal.repository.RatingRepository;
import platepal.repository.RestaurantRepository;
import platepal.repository.UserRepository;

/**
 * Following other users, looking at their lists, and viewing activity from the
 * people the current user follows.
 */
public class SocialService {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final RatingRepository ratingRepository;
    private final PersonalRankingService personalRankingService;
    private final AuthService authService;

    public SocialService(UserRepository userRepository,
                         RestaurantRepository restaurantRepository,
                         RatingRepository ratingRepository,
                         PersonalRankingService personalRankingService,
                         AuthService authService) {

        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
        this.ratingRepository = ratingRepository;
        this.personalRankingService = personalRankingService;
        this.authService = authService;
    }

    /**
     * Follows another user, named by username because that is what a person can
     * actually see and type.
     *
     * <p>The follow is one-way and needs no approval
     *
     * @return true if the current user was not already following them
     * @throws IllegalArgumentException if no such user exists, or it is the
     *                                  current user's own username
     * @throws IllegalStateException    if nobody is logged in
     */
    public boolean follow(String username) {
        User target = requireUser(username);
        User currentUser = authService.requireCurrentUser();

        boolean changed = currentUser.follow(target.getId());
        persist(currentUser);

        return changed;
    }

    /**
     * @return true if the current user had been following them
     * @throws IllegalArgumentException if no such user exists
     */
    public boolean unfollow(String username) {
        User target = requireUser(username);
        User currentUser = authService.requireCurrentUser();

        boolean changed = currentUser.unfollow(target.getId());
        persist(currentUser);

        return changed;
    }

    public boolean isFollowing(String username) {
        return findByUsername(username)
                .map(target -> authService.requireCurrentUser()
                        .isFollowing(target.getId()))
                .orElse(false);
    }

    /** @return the users the current user follows, in the order they were followed */
    public List<User> getFollowing() {
        return resolveUsers(authService.requireCurrentUser().getFollowingUserIds());
    }

    /**
     * @return the users who follow the current user
     *
     * <p>A user only stores who they follow, so the other direction has to be
     * found by scanning. That is fine at this size and keeps one source of truth
     * instead of two lists that could disagree.
     */
    public List<User> getFollowers() {
        String currentUserId = authService.requireCurrentUser().getId();

        return userRepository.findAll()
                .stream()
                .filter(user -> user.isFollowing(currentUserId))
                .toList();
    }

    /** @return every user except the one logged in, for browsing */
    public List<User> getOtherUsers() {
        String currentUserId = authService.requireCurrentUser().getId();

        return userRepository.findAll()
                .stream()
                .filter(user -> !user.getId().equals(currentUserId))
                .toList();
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Anyone may look at anyone else's lists
     *
     * @throws IllegalArgumentException if no such user exists
     */
    public List<Restaurant> getVisitedRestaurants(String username) {
        return resolveRestaurants(requireUser(username).getVisitedRestaurantIds());
    }

    /** @throws IllegalArgumentException if no such user exists */
    public List<Restaurant> getWantToTryRestaurants(String username) {
        return resolveRestaurants(requireUser(username).getWantToTryRestaurantIds());
    }

    /**
     * @return the given user's rated restaurants, highest score first
     * @throws IllegalArgumentException if no such user exists
     */
    public List<Restaurant> getHighestRatedRestaurants(String username) {
        User target = requireUser(username);

        return personalRankingService.getPersonalRanking(target.getId());
    }

    /**
     * Recent ratings left by users the current user follows, most recently
     * changed first.
     *
     * <p>Supports "View recent restaurant ratings from followed users". Each
     * {@link Rating} still stores only IDs, so the caller resolves usernames
     * and restaurant names as needed (see {@link #getUsername(String)}).
     *
     * @throws IllegalStateException if nobody is logged in
     */
    public List<Rating> getRecentActivityFromFollowedUsers() {
        User currentUser = authService.requireCurrentUser();

        List<Rating> activity = new ArrayList<>();
        for (String followedId : currentUser.getFollowingUserIds()) {
            activity.addAll(ratingRepository.findByUserId(followedId));
        }

        activity.sort(Comparator.comparing(Rating::getUpdatedAt).reversed());

        return activity;
    }

    /** @return the display name for a user ID, or the ID itself if the user is gone */
    public String getUsername(String userId) {
        return userRepository.findById(userId)
                .map(User::getUsername)
                .orElse(userId);
    }

    private User requireUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No user found with username " + username + "."));
    }

    private List<User> resolveUsers(Set<String> userIds) {
        List<User> users = new ArrayList<>();

        for (String id : userIds) {
            userRepository.findById(id).ifPresent(users::add);
        }

        return users;
    }

    private List<Restaurant> resolveRestaurants(Set<String> restaurantIds) {
        List<Restaurant> restaurants = new ArrayList<>();

        for (String id : restaurantIds) {
            restaurantRepository.findById(id).ifPresent(restaurants::add);
        }

        return restaurants;
    }

    private void persist(User user) {
        userRepository.save(user);
        authService.refreshCurrentUser();
    }
}
