package pw.rxj.iron_quarry.types;

import java.util.Arrays;
import pw.rxj.iron_quarry.compat.Compat;
import pw.rxj.iron_quarry.resource.config.util.ConfigFields;

public enum MonitorDepthOffsetMode {
    AUTO,
    PRECISE,
    STABLE;

    public static MonitorDepthOffsetMode byTranslationKey(String translationKey) {
        return Arrays.stream(values()).filter(monitorDepthOffsetMode -> monitorDepthOffsetMode.getTranslationKey().equals(translationKey)).findFirst().orElse(MonitorDepthOffsetMode.AUTO);
    }

    public String getTranslationKey() {
        return ConfigFields.getOptionName(this);
    }
    public MonitorDepthOffsetMode resolve() {
        if(this != AUTO) return this;

        return Compat.isShaderPackInUse() ? STABLE : PRECISE;
    }
}
