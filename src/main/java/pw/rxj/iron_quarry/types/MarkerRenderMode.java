package pw.rxj.iron_quarry.types;

import java.util.Arrays;

public enum MarkerRenderMode {
    HUD,
    WORLD;

    public static MarkerRenderMode byTranslationKey(String translationKey) {
        return Arrays.stream(values()).filter(mode -> mode.getTranslationKey().equals(translationKey)).findFirst().orElse(MarkerRenderMode.WORLD);
    }

    public String getTranslationKey() {
        return this.toString().toLowerCase();
    }
}
