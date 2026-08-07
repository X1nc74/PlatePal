package platepal.model;

/**
 * Distinguishes a regular user from an administrator.
 *
 * <p>The role is stored in JSON so that the persistence layer can rebuild the
 * correct Java subclass ({@link User} or {@link Administrator}) when loading data.
 * Permission checks in the service layer should rely on
 * {@link User#isAdministrator()} rather than reading this field directly.
 */
public enum Role {
    USER,
    ADMIN
}
