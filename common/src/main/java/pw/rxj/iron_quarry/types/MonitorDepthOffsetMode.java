package pw.rxj.iron_quarry.types;

import java.util.Arrays;
import pw.rxj.iron_quarry.compat.Compat;

public enum MonitorDepthOffsetMode {
    AUTO,
    PRECISE,
    STABLE;

    public static MonitorDepthOffsetMode byTranslationKey(String translationKey) {
        return Arrays.stream(values()).filter(monitorDepthOffsetMode -> monitorDepthOffsetMode.getTranslationKey().equals(translationKey)).findFirst().orElse(MonitorDepthOffsetMode.AUTO);
    }

    public String getTranslationKey() {
        return this.toString().toLowerCase();
    }
    public MonitorDepthOffsetMode resolve() {
        if(this != AUTO) return this;

        return Compat.isShaderPackInUse() ? STABLE : PRECISE;
    }
}
