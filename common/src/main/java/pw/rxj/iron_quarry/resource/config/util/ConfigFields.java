package pw.rxj.iron_quarry.resource.config.util;

import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.resource.config.Config;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ConfigFields {
    private ConfigFields() { }

    public static List<Field> of(Class<?> type) {
        List<Field> fields = new ArrayList<>();

        for(Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
            for(Field field : current.getDeclaredFields()) {
                int modifiers = field.getModifiers();
                if(Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers)) continue;

                fields.add(field);
            }
        }

        return fields;
    }
    public static @Nullable Field byName(Class<?> type, String name) {
        for(Field field : of(type)) {
            if(field.getName().equals(name)) return field;
        }

        return null;
    }
    public static boolean isConfigClass(Class<?> type) {
        return !type.isArray() && type.getPackageName().startsWith(Config.class.getPackageName());
    }
    public static @Nullable List<String> getAllowedOptions(Field field) {
        Options options = field.getAnnotation(Options.class);
        if(options == null) return null;

        return getOptionNames(options.value());
    }
    public static List<String> getOptionNames(Class<? extends Enum<?>> type) {
        return Arrays.stream(type.getEnumConstants()).map(ConfigFields::getOptionName).toList();
    }
    public static String getOptionName(Enum<?> constant) {
        return constant.toString().toLowerCase(Locale.ROOT);
    }
    public static String joinOptions(List<String> options) {
        return String.join(", ", options.stream().map(option -> "\"" + option + "\"").toList());
    }

    public static @Nullable Object get(Field field, @Nullable Object owner) {
        if(owner == null) return null;

        try {
            field.setAccessible(true);
            return field.get(owner);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Couldn't read config field " + field.getName(), e);
        }
    }
}
