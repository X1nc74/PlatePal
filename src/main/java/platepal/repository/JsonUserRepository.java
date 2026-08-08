package platepal.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import platepal.model.User;
import platepal.persistence.JsonDataStore;
import platepal.persistence.PlatePalData;

/**
 * Stores users in the shared {@code data/platepal-data.json} file, under the
 * {@code "users"} key described in section 13 of the alignment document.
 *
 * <p>Every method loads the whole file, works on it, and writes it back. That is
 * more work than a database would need, but it keeps the file consistent when
 * several repositories touch it in turn, and the data set for this project is
 * small enough that it does not matter.
 */
public class JsonUserRepository implements UserRepository {

    private final JsonDataStore dataStore;

    public JsonUserRepository() {
        this.dataStore = new JsonDataStore();
    }

    /** Lets tests point the repository at a temporary file. */
    public JsonUserRepository(JsonDataStore dataStore) {
        this.dataStore = dataStore;
    }

    @Override
    public List<User> findAll() {
        PlatePalData data = dataStore.load();
        return new ArrayList<>(data.getUsers());
    }

    @Override
    public Optional<User> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }

        return findAll()
                .stream()
                .filter(user -> user.getId().equals(id))
                .findFirst();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }

        String wanted = username.trim();

        return findAll()
                .stream()
                .filter(user -> user.getUsername().equalsIgnoreCase(wanted))
                .findFirst();
    }

    @Override
    public void save(User user) {
        PlatePalData data = dataStore.load();

        List<User> users = data.getUsers();

        Optional<User> existing = users.stream()
                .filter(u -> u.getId().equals(user.getId()))
                .findFirst();

        if (existing.isPresent()) {
            int index = users.indexOf(existing.get());
            users.set(index, user);
        } else {
            users.add(user);
        }

        dataStore.save(data);
    }

    @Override
    public void deleteById(String id) {
        PlatePalData data = dataStore.load();

        data.getUsers()
                .removeIf(u -> u.getId().equals(id));

        dataStore.save(data);
    }
}
