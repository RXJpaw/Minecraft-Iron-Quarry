package pw.rxj.iron_quarry.types;

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
}
