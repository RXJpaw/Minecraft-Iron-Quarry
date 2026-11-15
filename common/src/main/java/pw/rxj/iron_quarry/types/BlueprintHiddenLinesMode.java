package pw.rxj.iron_quarry.types;

import java.util.Arrays;
import pw.rxj.iron_quarry.compat.Compat;
import pw.rxj.iron_quarry.resource.config.util.ConfigFields;

public enum BlueprintHiddenLinesMode {
    AUTO,
    TRANSPARENT,
    DASHED;

    public static BlueprintHiddenLinesMode byTranslationKey(String translationKey) {
        return Arrays.stream(values()).filter(blueprintHiddenLinesMode -> blueprintHiddenLinesMode.getTranslationKey().equals(translationKey)).findFirst().orElse(BlueprintHiddenLinesMode.AUTO);
    }

    public String getTranslationKey() {
        return ConfigFields.getOptionName(this);
    }
    public BlueprintHiddenLinesMode resolve() {
        if(this != AUTO) return this;

        return Compat.isShaderPackInUse() ? DASHED : TRANSPARENT;
    }
}
