package pw.rxj.iron_quarry.types;

import java.util.List;

public enum AugmentType {
    EMPTY(0, "empty", 1, 0.0F, 0.0F),
    SPEED(1, "speed", 1000, 0.10F, 0.05F),
    FORTUNE(2, "fortune", 1500, 2.0F/225.0F, 1.0F/75.0F);

    private final int id;
    private final String name;
    private final int baseAmount;
    private final float multiplier;
    private final float inefficiency;

    private static final List<AugmentType> ALL = List.of(EMPTY, SPEED, FORTUNE);

    private AugmentType(int id, String name, int baseAmount, float multiplier, float inefficiency) {
        this.id = id;
        this.name = name;
        this.baseAmount = baseAmount;
        this.multiplier = multiplier;
        this.inefficiency = inefficiency;
    }

    public String getName(){
        return this.name;
    }
    public int getId(){
        return this.id;
    }
    public int getBaseAmount() { return this.baseAmount; }
    public float getMultiplier() { return this.multiplier; }
    public float getInefficiency() { return this.inefficiency; }

    public boolean isEmpty() { return this.equals(AugmentType.EMPTY); }
    public boolean isPresent() { return !this.isEmpty(); }

    public static AugmentType from(String name){
        for (AugmentType augmentType : ALL) {
            if(augmentType.getName().equalsIgnoreCase(name)){
                return augmentType;
            }
        }

        return null;
    }
    public static AugmentType from(int id){
        return ALL.get(id);
    }
}
