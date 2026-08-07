package platepal.model;

/**
 * An administrator is a user with the additional right to manage core restaurant
 * data. This is a genuine "is-a" relationship: an administrator registers, logs
 * in, keeps personal lists and follows other users exactly like any other user.
 *
 * <p>Only the permission behaviour differs, which is why the single overridden
 * method {@link #isAdministrator()} is enough. The service layer asks the object
 * what it may do instead of inspecting its type, so adding a future role would
 * not require changing every service.
 */
public class Administrator extends User {

    /** Required by Gson. Do not use directly in application code. */
    protected Administrator() {
        super();
    }

    public Administrator(String id, String username, String password) {
        super(id, username, password, Role.ADMIN);
    }

    @Override
    public boolean isAdministrator() {
        return true;
    }
}
