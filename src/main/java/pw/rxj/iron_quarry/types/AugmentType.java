package pw.rxj.iron_quarry.types;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;
import org.jetbrains.annotations.NotNull;
import pw.rxj.iron_quarry.item.AugmentItem;
import pw.rxj.iron_quarry.resource.config.server.AugmentStatsConfig;
import pw.rxj.iron_quarry.util.MathUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.IntFunction;

public enum AugmentType implements StringIdentifiable {
    EMPTY(0, "empty", "energy", 1, 0, 0.0F, false),
    SPEED(1, "speed", "energy", 1000, 1500, 10.0F/6.0F, false),
    FORTUNE(2, "fortune", "energy", 1500, 200, 10.0F, false),
    SILK_TOUCH(3, "silk_touch", "speed", 0, 0, 2.0F, false),
    CHEST_LOOTING(4, "chest_looting", "time", 0, 0, 0.0F, false);

    public static final Codec<AugmentType> CODEC = StringIdentifiable.createBasicCodec(AugmentType::values);
    public static final IntFunction<AugmentType> ID_TO_VALUE = ValueLists.<AugmentType>createIdToValueFunction((value) -> value.id, values(), ValueLists.OutOfBoundsHandling.ZERO);
    public static final PacketCodec<ByteBuf, AugmentType> PACKET_CODEC = PacketCodecs.indexed(ID_TO_VALUE, (value) -> value.id);

    private final int id;
    private final String name;
    private final String drawbackKey;
    private int baseAmount;
    private int maxEfficiency;
    private float inefficiency;
    private boolean disabled;

    private List<Integer> capacities;
    private double multiplier;

    private AugmentType(int id, String name, String drawbackKey, int baseAmount, int maxEfficiency, float inefficiency, boolean disabled) {
        this.id = id;
        this.name = name;
        this.drawbackKey = drawbackKey;

        this.baseAmount = baseAmount;
        this.maxEfficiency = maxEfficiency;
        this.inefficiency = inefficiency;
        this.disabled = disabled;

        this.cacheCapacities();
        this.cacheMultiplier();
    }
    public void override(AugmentStatsConfig.Entry augmentStats) {
        this.baseAmount = augmentStats.baseAmount;
        this.maxEfficiency = augmentStats.maxEfficiency;
        this.inefficiency = augmentStats.inefficiency;
        this.disabled = augmentStats.disabled;

        this.cacheCapacities();
        this.cacheMultiplier();
    }

    /**
     *  C(U) = B * (1 + 0.56 * U * (U + 1))
     */
    private void cacheCapacities() {
        this.capacities = new ArrayList<>();

        for (int i = 0; i <= AugmentItem.CAPACITY_UPGRADE_SLOTS; i++) {
            this.capacities.add(i, Math.round(this.getBaseAmount() * (1 + 0.56F * i * (i + 1))));
        }
    }
    /**
     *  R(A) = log_2(log_2(2 + A / 10 000)) * M
     */
    private void cacheMultiplier() {
        this.multiplier = this.maxEfficiency == 0 ? 0 : this.maxEfficiency / MathUtil.log2(MathUtil.log2(2 + this.getCapacity(this.capacities.size() - 1) / 10_000d));
    }

    public int getId(){
        return this.id;
    }
    public String getName(){
        return this.name;
    }
    public String getDrawbackKey() {
        return drawbackKey;
    }

    public int getBaseAmount() { return this.baseAmount; }
    public int getMaxEfficiency() { return this.maxEfficiency; }
    public float getInefficiency() { return this.inefficiency; }
    public boolean isDisabled() {
        return this.disabled;
    }

    public boolean isEmpty() { return this.equals(AugmentType.EMPTY); }
    public boolean isPresent() { return !this.isEmpty(); }

    public int getCapacity(int capacityUpgrades) {
        return Objects.requireNonNull(this.capacities.get(capacityUpgrades));
    }

    public float multiply(int amount) {
        if(this.multiplier == 0) return 0.0F;

        return (float) (MathUtil.log2(MathUtil.log2(2 + amount / 10_000d)) * this.multiplier);
    }

    public float anyIneff(float amount) {
        if(this.multiplier == 0) return this.inefficiency;

        return amount * this.inefficiency;
    }
    public float energyIneff(float amount) {
        if(!this.drawbackKey.equals("energy")) return 0;

        return this.anyIneff(amount);
    }
    public float speedIneff(float amount) {
        if(!this.drawbackKey.equals("speed")) return amount;

        return this.anyIneff(amount);
    }

    public static @NotNull AugmentType from(String name){
        for (AugmentType augmentType : values()) {
            if(augmentType.getName().equalsIgnoreCase(name)){
                return augmentType;
            }
        }

        return AugmentType.EMPTY;
    }

    @Override
    public String asString() {
        return this.getName();
    }
}
