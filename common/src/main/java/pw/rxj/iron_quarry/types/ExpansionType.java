package pw.rxj.iron_quarry.types;

import pw.rxj.iron_quarry.expansion.AbstractExpansion;
import pw.rxj.iron_quarry.expansion.ZExpansions;

public enum ExpansionType {
    INTERDIMENSIONAL("interdimensional"),
    MINING_PATTERN("mining_pattern");

    private final String name;

    private ExpansionType(String name) {
        this.name = name;
    }

    public String getName(){
        return this.name;
    }

    public AbstractExpansion getExpansion() {
        return switch (this) {
            case INTERDIMENSIONAL -> ZExpansions.INTERDIMENSIONAL;
            case MINING_PATTERN -> ZExpansions.MINING_PATTERN;
        };
    }
}
