package pw.rxj.iron_quarry.types;

import java.util.Arrays;
import pw.rxj.iron_quarry.resource.config.util.ConfigFields;

public enum DimensionListMode {
    BLACKLIST,
    WHITELIST;

    public static DimensionListMode byOptionName(String optionName) {
        return Arrays.stream(values()).filter(dimensionListMode -> dimensionListMode.getOptionName().equals(optionName)).findFirst().orElse(DimensionListMode.BLACKLIST);
    }

    public String getOptionName() {
        return ConfigFields.getOptionName(this);
    }
}
