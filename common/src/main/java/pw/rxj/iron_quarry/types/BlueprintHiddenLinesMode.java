package pw.rxj.iron_quarry.types;

import java.util.Arrays;
import pw.rxj.iron_quarry.compat.Compat;

public enum BlueprintHiddenLinesMode {
    AUTO,
    TRANSPARENT,
    DASHED;

    public static BlueprintHiddenLinesMode byTranslationKey(String translationKey) {
        return Arrays.stream(values()).filter(blueprintHiddenLinesMode -> blueprintHiddenLinesMode.getTranslationKey().equals(translationKey)).findFirst().orElse(BlueprintHiddenLinesMode.AUTO);
    }

    public String getTranslationKey() {
        return this.toString().toLowerCase();
    }
    public BlueprintHiddenLinesMode resolve() {
        if(this != AUTO) return this;

        return Compat.isShaderPackInUse() ? DASHED : TRANSPARENT;
    }
}
