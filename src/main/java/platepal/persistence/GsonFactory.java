package platepal.persistence;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import platepal.model.Administrator;
import platepal.model.Role;
import platepal.model.User;

/**
 * Builds the single configured {@link Gson} instance used by the whole project.
 *
 * <p>Two problems have to be solved before Gson can round-trip the PlatePal
 * model, and both are easy to miss until the data file fails to load:
 *
 * <ol>
 *   <li><b>LocalDateTime.</b> Out of the box Gson writes the internal fields of
 *       LocalDateTime instead of a readable timestamp, and on Java 17 it throws
 *       an InaccessibleObjectException because java.time is not open for
 *       reflection. A type adapter writes ISO-8601 text instead, matching the
 *       "2026-08-04T10:30:00" format in the alignment document.</li>
 *   <li><b>Administrator.</b> Gson deserializes a {@code List<User>} into plain
 *       User objects, so an administrator loaded from disk would silently lose
 *       its admin rights. The deserializer below reads the "role" field and
 *       creates the correct subclass.</li>
 * </ol>
 */
public final class GsonFactory {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private GsonFactory() {
    }

    public static Gson create() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer())
                .registerTypeAdapter(User.class, new UserDeserializer())
                .create();
    }

    private static class LocalDateTimeSerializer implements JsonSerializer<LocalDateTime> {
        @Override
        public JsonElement serialize(LocalDateTime value,
                                     Type type,
                                     JsonSerializationContext context) {
            return value == null ? null : new JsonPrimitive(FORMATTER.format(value));
        }
    }

    private static class LocalDateTimeDeserializer implements JsonDeserializer<LocalDateTime> {
        @Override
        public LocalDateTime deserialize(JsonElement element,
                                         Type type,
                                         JsonDeserializationContext context) {
            if (element == null || element.isJsonNull()) {
                return null;
            }
            return LocalDateTime.parse(element.getAsString(), FORMATTER);
        }
    }

    /**
     * Chooses the concrete class based on the stored role. Calling
     * {@code context.deserialize} with the concrete type is safe here because
     * that type has no adapter registered, so there is no infinite recursion.
     */
    private static class UserDeserializer implements JsonDeserializer<User> {
        @Override
        public User deserialize(JsonElement element,
                                Type type,
                                JsonDeserializationContext context) {
            JsonObject object = element.getAsJsonObject();
            JsonElement roleElement = object.get("role");
            String role = roleElement == null || roleElement.isJsonNull()
                    ? Role.USER.name()
                    : roleElement.getAsString();

            User user = Role.ADMIN.name().equalsIgnoreCase(role)
                    ? context.deserialize(element, Administrator.class)
                    : context.deserialize(element, RegularUser.class);

            user.repairAfterDeserialization();
            return user;
        }
    }

    /**
     * Marker subclass with no extra state. It exists only so the deserializer
     * can ask Gson for a concrete type that is not {@code User} itself, which
     * would re-enter the adapter above.
     */
    private static class RegularUser extends User {
        RegularUser() {
            super();
        }
    }
}
