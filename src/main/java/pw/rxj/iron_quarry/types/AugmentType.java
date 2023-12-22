package pw.rxj.iron_quarry.types;

import pw.rxj.iron_quarry.resource.Config;

import java.util.List;

public enum AugmentType {
    EMPTY(0, "empty", "energy", 1, 0.0F, 0.0F),
    SPEED(1, "speed", "energy", 1000, 0.10F, 0.05F),
    FORTUNE(2, "fortune", "energy", 1500, 2.0F/225.0F, 1.0F/75.0F),
    SILK_TOUCH(3, "silk_touch", "energy", 0, 0.0F, 180.0F),
    CHEST_LOOTING(4, "chest_looting", "time", 0, 0.0F, 0.0F);

    private final int id;
    private final String name;
    private final String drawbackKey;
    private int baseAmount;
    private float multiplier;
    private float inefficiency;

    private static final List<AugmentType> ALL = List.of(EMPTY, SPEED, FORTUNE, SILK_TOUCH);

    private AugmentType(int id, String name, String drawbackKey, int baseAmount, float multiplier, float inefficiency) {
        this.id = id;
        this.name = name;
        this.drawbackKey = drawbackKey;
        this.baseAmount = baseAmount;
        this.multiplier = multiplier;
        this.inefficiency = inefficiency;
    }
    public void override(Config.Server.AugmentStatsConfig.Entry augmentStats) {
        this.baseAmount = augmentStats.baseAmount;
        this.multiplier = augmentStats.multiplier;
        this.inefficiency = augmentStats.inefficiency;
    }

    public String getName(){
        return this.name;
    }
    public String getDrawbackKey() {
        return drawbackKey;
    }
    public int getId(){
        return this.id;
    }
    public int getBaseAmount() { return this.baseAmount; }
    public float getMultiplier() { return this.multiplier; }
    public float getInefficiency() { return this.inefficiency; }

    public boolean isEmpty() { return this.equals(AugmentType.EMPTY); }
    public boolean isPresent() { return !this.isEmpty(); }

    /**
     *  C(U) = B * (1 + 0.25 * U * (U + 1))
     */
    public int getCapacity(int capacityUpgrades) {
        return (int) (this.getBaseAmount() * (1 + 0.25F * capacityUpgrades * (capacityUpgrades + 1)));
    }

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
