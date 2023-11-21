package pw.rxj.iron_quarry;

import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.util.ZUtil;

import java.awt.*;
import java.lang.reflect.Field;

public class Global {
    public static final int RGB_BENEFIT = Color.HSBtoRGB(ZUtil.normDeg(120), 0.4F, 0.5F);
    public static final int RGB_DRAWBACK = Color.HSBtoRGB(ZUtil.normDeg(0), 0.4F, 0.5F);
    public static final int RGB_DARK_GRAY = Color.HSBtoRGB(ZUtil.normDeg(0), 0.0F, 1.0F/3.0F);
    public static final int RGB_LIGHT_GRAY = Color.HSBtoRGB(ZUtil.normDeg(0), 0.0F, 0.6F);
    public static final int RGB_WEAK_HIGHLIGHT = Color.HSBtoRGB(ZUtil.normDeg(210), 0.4F, 0.5F);
    public static final int RGB_STRONG_HIGHLIGHT = Color.HSBtoRGB(ZUtil.normDeg(210), 0.6F, 0.5F);

    public static @Nullable Object get(String name){
        try {
            Field field = Global.class.getField(name);

            return field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
            return null;
        }
    }
}
