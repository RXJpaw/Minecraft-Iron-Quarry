package pw.rxj.iron_quarry.resource.config.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class JsoncWriter {
    private static final String INDENT = "  ";

    private final Gson gson;
    private final StringBuilder output = new StringBuilder();
    private final Set<Field> describedFields = new HashSet<>();

    private JsoncWriter(Gson gson) {
        this.gson = gson;
    }

    public static String write(Gson gson, String header, Object config, Object defaults) {
        JsoncWriter writer = new JsoncWriter(gson);

        header.lines().forEach(line -> writer.writeComment(line, 0));
        writer.writeElement(gson.toJsonTree(config), config.getClass(), defaults, 0);
        writer.output.append('\n');

        return writer.output.toString();
    }

    private void writeElement(JsonElement element, Class<?> type, @Nullable Object defaults, int depth) {
        if(element.isJsonObject()) {
            this.writeObject(element.getAsJsonObject(), type, defaults, depth);
        } else if(element.isJsonArray()) {
            this.writeArray(element.getAsJsonArray(), type.isArray() ? type.getComponentType() : Object.class, depth);
        } else {
            this.output.append(this.gson.toJson(element));
        }
    }
    private void writeObject(JsonObject object, Class<?> type, @Nullable Object defaults, int depth) {
        if(object.isEmpty()) {
            this.output.append("{}");
            return;
        }

        this.output.append("{\n");

        Iterator<Map.Entry<String, JsonElement>> iterator = object.entrySet().iterator();
        while(iterator.hasNext()) {
            Map.Entry<String, JsonElement> entry = iterator.next();
            Field field = ConfigFields.byName(type, entry.getKey());
            Object fieldDefault = field == null ? null : ConfigFields.get(field, defaults);

            this.writeDescription(field, fieldDefault, entry.getValue(), depth + 1);
            this.indent(depth + 1);
            this.output.append(this.gson.toJson(entry.getKey())).append(": ");
            this.writeElement(entry.getValue(), field == null ? Object.class : field.getType(), fieldDefault, depth + 1);
            if(iterator.hasNext()) this.output.append(',');
            this.output.append('\n');
        }

        this.indent(depth);
        this.output.append('}');
    }
    private void writeArray(JsonArray array, Class<?> componentType, int depth) {
        if(array.isEmpty()) {
            this.output.append("[]");
            return;
        }

        this.output.append("[\n");

        for(int i = 0; i < array.size(); i++) {
            this.indent(depth + 1);
            this.writeElement(array.get(i), componentType, null, depth + 1);
            if(i < array.size() - 1) this.output.append(',');
            this.output.append('\n');
        }

        this.indent(depth);
        this.output.append(']');
    }

    private void writeDescription(@Nullable Field field, @Nullable Object fieldDefault, JsonElement value, int depth) {
        if(field == null) return;

        if(this.describedFields.add(field)) {
            Comment comment = field.getAnnotation(Comment.class);
            if(comment != null) comment.value().lines().forEach(line -> this.writeComment(line, depth));

            String rangeText = describeRange(field.getAnnotation(Range.class));
            if(rangeText != null) this.writeComment(rangeText, depth);
        }

        if(fieldDefault != null && value.isJsonPrimitive()) {
            this.writeComment("Default: " + this.gson.toJson(fieldDefault), depth);
        }
    }
    private void writeComment(String text, int depth) {
        this.indent(depth);
        this.output.append("// ").append(text).append('\n');
    }
    private void indent(int depth) {
        this.output.repeat(INDENT, depth);
    }

    private static @Nullable String describeRange(@Nullable Range range) {
        if(range == null) return null;

        boolean hasMin = range.min() != Double.NEGATIVE_INFINITY;
        boolean hasMax = range.max() != Double.POSITIVE_INFINITY;

        if(hasMin && hasMax) return "Range: " + formatRangeLimit(range.min()) + " to " + formatRangeLimit(range.max());
        if(hasMin) return "Minimum: " + formatRangeLimit(range.min());
        if(hasMax) return "Maximum: " + formatRangeLimit(range.max());

        return null;
    }
    private static String formatRangeLimit(double value) {
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }
}
