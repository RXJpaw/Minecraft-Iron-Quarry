package pw.rxj.iron_quarry.resource.config.util;

import com.google.gson.*;
import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.Main;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsoncReader {
    private static final int MAX_SUGGESTION_TYPOS = 2;

    private static final String QUOTED_TEXT_REGEX = "\"[^\"\\\\]*+(?:\\\\.[^\"\\\\]*+)*+\"|'[^'\\\\]*+(?:\\\\.[^'\\\\]*+)*+'";
    private static final String COMMENT_REGEX = "//[^\\r\\n]*+|#[^\\r\\n]*+|/\\*.*?\\*/";
    private static final String TRAILING_COMMA_REGEX = ",(?=(?:\\s++|" + COMMENT_REGEX + ")*+[}\\]])";
    private static final Pattern TRAILING_COMMA_SEARCH_PATTERN = Pattern.compile(QUOTED_TEXT_REGEX + "|" + COMMENT_REGEX + "|" + TRAILING_COMMA_REGEX, Pattern.DOTALL);

    private JsoncReader() { }

    public static <T> T read(Gson gson, String text, Class<T> type, T defaults) {
        JsonElement element = JsonParser.parseString(removeTrailingCommas(text));
        if(element.isJsonNull()) throw new JsonParseException("File is empty");
        if(!element.isJsonObject()) throw new JsonParseException("Expected an object at the top of the file, but was " + element);

        JsonObject checked = checkObject(element.getAsJsonObject(), gson.toJsonTree(defaults).getAsJsonObject(), type, "");

        return gson.fromJson(checked, type);
    }

    private static @Nullable JsonObject checkObject(JsonObject object, @Nullable JsonObject defaults, Class<?> type, String path) {
        JsonObject checked = new JsonObject();
        boolean complete = true;

        for(Field field : ConfigFields.of(type)) {
            String name = field.getName();
            JsonElement value = checkField(object.get(name), defaults == null ? null : defaults.get(name), field, path.isEmpty() ? name : path + "." + name);

            if(value == null) {
                complete = false;
            } else {
                checked.add(name, value);
            }
        }

        for(String key : object.keySet()) {
            if(ConfigFields.byName(type, key) != null) continue;

            String keyPath = path.isEmpty() ? key : path + "." + key;
            String suggestion = suggestFieldName(type, key);

            if(suggestion == null) {
                Main.LOGGER.warn("Config key {} is unknown and ignored.", keyPath);
            } else {
                Main.LOGGER.warn("Config key {} is unknown and ignored, did you mean {}?", keyPath, suggestion);
            }
        }

        return complete ? checked : null;
    }
    private static @Nullable JsonElement checkField(@Nullable JsonElement value, @Nullable JsonElement defaultValue, Field field, String path) {
        if(value == null) {
            if(defaultValue == null) {
                Main.LOGGER.warn("Config value {} is missing.", path);
            } else {
                Main.LOGGER.info("Config value {} is missing, using {}.", path, describeDefaultValue(defaultValue));
            }

            return defaultValue;
        }

        JsonElement checked = checkValue(value, defaultValue, field, field.getType(), path);
        if(checked != null || isConfigObject(value, field.getType())) return checked;

        if(defaultValue == null) {
            Main.LOGGER.warn("Config value {} = {} is invalid, expected {}.", path, value, describeExpectedType(field.getType()));
        } else {
            Main.LOGGER.warn("Config value {} = {} is invalid, expected {}, using {}.", path, value, describeExpectedType(field.getType()), describeDefaultValue(defaultValue));
        }

        return defaultValue;
    }
    private static @Nullable JsonElement checkValue(JsonElement value, @Nullable JsonElement defaultValue, Field field, Class<?> type, String path) {
        if(type.isArray()) {
            return value.isJsonArray() ? checkArray(value.getAsJsonArray(), field, type.getComponentType(), path) : null;
        }
        if(ConfigFields.isConfigClass(type)) {
            if(!value.isJsonObject()) return null;

            return checkObject(value.getAsJsonObject(), defaultValue != null && defaultValue.isJsonObject() ? defaultValue.getAsJsonObject() : null, type, path);
        }
        if(!value.isJsonPrimitive()) return null;

        JsonPrimitive primitive = value.getAsJsonPrimitive();
        if(isBooleanType(type)) return primitive.isBoolean() ? primitive : null;
        if(type == String.class) return primitive.isString() ? primitive : null;

        NumberType numberType = NumberType.of(type);
        if(numberType != null) return checkNumber(primitive, field.getAnnotation(Range.class), numberType, path);

        return primitive;
    }
    private static JsonArray checkArray(JsonArray array, Field field, Class<?> componentType, String path) {
        JsonArray checked = new JsonArray();

        for(int i = 0; i < array.size(); i++) {
            JsonElement entry = array.get(i);
            String entryPath = path + "[" + i + "]";
            JsonElement checkedEntry = checkValue(entry, null, field, componentType, entryPath);

            if(checkedEntry != null) {
                checked.add(checkedEntry);
            } else if(isConfigObject(entry, componentType)) {
                Main.LOGGER.warn("Config list entry {} is incomplete and ignored.", entryPath);
            } else {
                Main.LOGGER.warn("Config list entry {} = {} is invalid and ignored, expected {}.", entryPath, entry, describeExpectedType(componentType));
            }
        }

        return checked;
    }
    private static @Nullable JsonPrimitive checkNumber(JsonPrimitive primitive, @Nullable Range range, NumberType type, String path) {
        BigDecimal number;
        try {
            number = new BigDecimal(primitive.getAsString().trim());
        } catch (NumberFormatException e) {
            return null;
        }

        if(type.isWhole() && number.stripTrailingZeros().scale() > 0) return null;

        BigDecimal clamped = number.max(type.getLowestAllowedValue(range)).min(type.getHighestAllowedValue(range));
        JsonPrimitive checked = type.toJsonNumber(clamped);

        if(clamped.compareTo(number) != 0) {
            Main.LOGGER.warn("Config value {} = {} is out of range, using {}.", path, primitive, checked);
        }

        return checked;
    }

    private static boolean isConfigObject(JsonElement value, Class<?> type) {
        return value.isJsonObject() && ConfigFields.isConfigClass(type);
    }
    private static boolean isBooleanType(Class<?> type) {
        return type == boolean.class || type == Boolean.class;
    }

    private static String describeDefaultValue(JsonElement defaultValue) {
        return defaultValue.isJsonPrimitive() ? defaultValue.toString() : "the defaults";
    }
    private static String describeExpectedType(Class<?> type) {
        if(isBooleanType(type)) return "true or false";

        NumberType numberType = NumberType.of(type);
        if(numberType != null) return numberType.isWhole() ? "a whole number" : "a number";

        if(type == String.class) return "a text in quotes";
        if(type.isArray()) return "a list";
        if(ConfigFields.isConfigClass(type)) return "an object";

        return type.getSimpleName();
    }

    private static @Nullable String suggestFieldName(Class<?> type, String key) {
        String suggestion = null;
        int fewestTypos = MAX_SUGGESTION_TYPOS + 1;

        for(Field field : ConfigFields.of(type)) {
            int typos = countTypos(key.toLowerCase(Locale.ROOT), field.getName().toLowerCase(Locale.ROOT));

            if(typos < fewestTypos) {
                fewestTypos = typos;
                suggestion = field.getName();
            }
        }

        return suggestion;
    }
    private static int countTypos(String key, String fieldName) {
        int[] previous = new int[fieldName.length() + 1];
        int[] current = new int[fieldName.length() + 1];

        for(int j = 0; j <= fieldName.length(); j++) previous[j] = j;

        for(int i = 1; i <= key.length(); i++) {
            current[0] = i;

            for(int j = 1; j <= fieldName.length(); j++) {
                int cost = key.charAt(i - 1) == fieldName.charAt(j - 1) ? 0 : 1;
                current[j] = Math.min(Math.min(current[j - 1] + 1, previous[j] + 1), previous[j - 1] + cost);
            }

            int[] swap = previous;
            previous = current;
            current = swap;
        }

        return previous[fieldName.length()];
    }

    private static String removeTrailingCommas(String text) {
        return TRAILING_COMMA_SEARCH_PATTERN.matcher(text).replaceAll(match -> match.group().equals(",") ? " " : Matcher.quoteReplacement(match.group()));
    }

    private enum NumberType {
        INT(true, BigDecimal.valueOf(Integer.MIN_VALUE), BigDecimal.valueOf(Integer.MAX_VALUE), BigDecimal::intValue),
        LONG(true, BigDecimal.valueOf(Long.MIN_VALUE), BigDecimal.valueOf(Long.MAX_VALUE), BigDecimal::longValue),
        FLOAT(false, BigDecimal.valueOf(-Float.MAX_VALUE), BigDecimal.valueOf(Float.MAX_VALUE), BigDecimal::floatValue),
        DOUBLE(false, BigDecimal.valueOf(-Double.MAX_VALUE), BigDecimal.valueOf(Double.MAX_VALUE), BigDecimal::doubleValue);

        private final boolean whole;
        private final BigDecimal lowest;
        private final BigDecimal highest;
        private final Function<BigDecimal, Number> converter;

        private NumberType(boolean whole, BigDecimal lowest, BigDecimal highest, Function<BigDecimal, Number> converter) {
            this.whole = whole;
            this.lowest = lowest;
            this.highest = highest;
            this.converter = converter;
        }

        private static @Nullable NumberType of(Class<?> type) {
            if(type == int.class || type == Integer.class) return INT;
            if(type == long.class || type == Long.class) return LONG;
            if(type == float.class || type == Float.class) return FLOAT;
            if(type == double.class || type == Double.class) return DOUBLE;

            return null;
        }

        private boolean isWhole() {
            return this.whole;
        }
        private BigDecimal getLowestAllowedValue(@Nullable Range range) {
            return range != null && Double.isFinite(range.min()) ? this.lowest.max(BigDecimal.valueOf(range.min())) : this.lowest;
        }
        private BigDecimal getHighestAllowedValue(@Nullable Range range) {
            return range != null && Double.isFinite(range.max()) ? this.highest.min(BigDecimal.valueOf(range.max())) : this.highest;
        }
        private JsonPrimitive toJsonNumber(BigDecimal number) {
            return new JsonPrimitive(this.converter.apply(number));
        }
    }
}
