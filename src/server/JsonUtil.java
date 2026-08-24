package server;

import com.google.gson.*;
import model.Administrator;
import model.DentistUser;
import model.Receptionist;
import model.User;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * JSON serialization and deserialization utility configured with Java Time adapters
 * and polymorphic User type handling.
 *
 * @author Student
 */
public final class JsonUtil {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, (JsonSerializer<LocalDate>) (src, typeOfSrc, context) ->
                    new JsonPrimitive(src.format(DATE_FORMATTER)))
            .registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (json, typeOfT, context) ->
                    LocalDate.parse(json.getAsString(), DATE_FORMATTER))
            .registerTypeAdapter(LocalTime.class, (JsonSerializer<LocalTime>) (src, typeOfSrc, context) ->
                    new JsonPrimitive(src.format(TIME_FORMATTER)))
            .registerTypeAdapter(LocalTime.class, (JsonDeserializer<LocalTime>) (json, typeOfT, context) -> {
                String val = json.getAsString();
                if (val.length() == 5) { // HH:mm format
                    val = val + ":00";
                }
                return LocalTime.parse(val, TIME_FORMATTER);
            })
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) ->
                    new JsonPrimitive(src.format(DATETIME_FORMATTER)))
            .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) ->
                    LocalDateTime.parse(json.getAsString(), DATETIME_FORMATTER))
            .registerTypeAdapter(User.class, new UserJsonDeserializer())
            .setPrettyPrinting()
            .create();

    private JsonUtil() {
    }

    public static Gson getGson() {
        return GSON;
    }

    public static String toJson(Object obj) {
        return GSON.toJson(obj);
    }

    public static <T> T fromJson(String json, Class<T> classOfT) {
        return GSON.fromJson(json, classOfT);
    }

    public static <T> T fromJson(String json, Type typeOfT) {
        return GSON.fromJson(json, typeOfT);
    }

    /**
     * Polymorphic User deserializer based on role attribute.
     */
    private static class UserJsonDeserializer implements JsonDeserializer<User> {
        @Override
        public User deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            String role = jsonObject.has("role") ? jsonObject.get("role").getAsString() : "";
            if ("Administrator".equalsIgnoreCase(role)) {
                return context.deserialize(jsonObject, Administrator.class);
            } else if ("Dentist".equalsIgnoreCase(role)) {
                return context.deserialize(jsonObject, DentistUser.class);
            } else {
                return context.deserialize(jsonObject, Receptionist.class);
            }
        }
    }
}
