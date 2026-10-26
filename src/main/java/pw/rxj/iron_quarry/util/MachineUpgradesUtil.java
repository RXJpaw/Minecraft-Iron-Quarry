package pw.rxj.iron_quarry.util;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import pw.rxj.iron_quarry.block.QuarryBlock;
import pw.rxj.iron_quarry.item.AugmentItem;
import pw.rxj.iron_quarry.item.DrillItem;
import pw.rxj.iron_quarry.types.AugmentType;

import java.util.List;
import java.util.function.Consumer;

public class MachineUpgradesUtil {
    public static final float FORTUNE_LIMIT = 3.0F;
    public static final float SPEED_LIMIT = 91.0F + 2.71293F;

    private boolean hasSilkTouch = false;
    private boolean hasChestLooting = false;

    private float fortuneMultiplier = 1;
    private float speedMultiplier = 1;
    private float inefficiency = 1;
    private float slowness = 1;

    private MachineUpgradesUtil(Container inventory){
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);

            if (stack.getItem() instanceof AugmentItem augmentItem) {
                AugmentType augmentType = augmentItem.getType(stack);
                if(augmentType.isDisabled()) continue;

                float multiplier = augmentType.multiply(augmentItem.getAmount(stack));
                float inefficiency = augmentType.energyIneff(multiplier);

                switch (augmentType) {
                    case SPEED -> this.speedMultiplier += multiplier/100;
                    case FORTUNE -> this.fortuneMultiplier += multiplier/100;
                    case SILK_TOUCH -> {
                        if(this.hasSilkTouch) continue;
                        this.hasSilkTouch = true;
                    }
                    case CHEST_LOOTING -> {
                        if(this.hasChestLooting) continue;
                        this.hasChestLooting = true;
                    }
                }

                this.inefficiency += inefficiency/100;
                this.slowness = augmentType.speedIneff(this.slowness);
            } else if(stack.getItem() instanceof DrillItem) {
                int efficiencyLevel = ZUtil.getCappedEnchantmentLevel(stack, Enchantments.EFFICIENCY);
                if(efficiencyLevel <= 0) continue;

                float speedMultiplier = (float) Math.pow(1.3, efficiencyLevel) - 1.0F;

                this.inefficiency += AugmentType.SPEED.energyIneff(speedMultiplier);
                this.speedMultiplier += speedMultiplier;
            }
        }
    }

    public static MachineUpgradesUtil from(Container inventory) {
        return new MachineUpgradesUtil(inventory);
    }
    public static MachineUpgradesUtil from(List<ItemStack> stacks) {
        return from(new SimpleContainer(stacks.toArray(new ItemStack[0])));
    }

    public boolean hasSilkTouch() { return this.hasSilkTouch; }
    public boolean hasChestLooting() { return this.hasChestLooting; }

    public float getFortuneMultiplier() {
        return Math.min(this.fortuneMultiplier, FORTUNE_LIMIT);
    }
    public float getSpeedMultiplier() {
        return Math.min(this.speedMultiplier / this.slowness, SPEED_LIMIT);
    }
    public float getInefficiency() {
        return this.inefficiency;
    }

    //conflicts
    public static final int CONFLICT_SPEED = 0;
    public static final int CONFLICT_FORTUNE = 1;
    public static final int CONFLICT_SILK_TOUCH = 2;
    public static boolean hasConflicts(int flag) {
        return flag != 0;
    }
    public static boolean hasConflict(int flag, int conflict) {
        conflict = 1 << conflict;

        return (flag & conflict) == conflict;
    }

    public boolean[] getConflictList() {
        return new boolean[] { this.exceedsSpeedLimit(), this.exceedsFortuneLimit(), this.conflictsWithSilkTouch() };
    }
    public int getConflictAmount() {
        int amount = 0;

        for (boolean hasConflict : this.getConflictList()) {
            if(hasConflict) amount++;
        }

        return amount;
    }
    public int getConflictFlag() {
        boolean[] list = this.getConflictList();
        int flag = 0;

        if(list[CONFLICT_SPEED]) flag += 1 << CONFLICT_SPEED;
        if(list[CONFLICT_FORTUNE]) flag += 1 << CONFLICT_FORTUNE;
        if(list[CONFLICT_SILK_TOUCH]) flag += 1 << CONFLICT_SILK_TOUCH;

        return flag;
    }
    public boolean hasConflicts() {
        return hasConflicts(this.getConflictFlag());
    }

    public boolean exceedsSpeedLimit() {
        return this.speedMultiplier > SPEED_LIMIT;
    }
    public boolean exceedsFortuneLimit() {
        return this.fortuneMultiplier > FORTUNE_LIMIT;
    }
    public boolean conflictsWithSilkTouch() {
        return this.hasSilkTouch && this.fortuneMultiplier > 1;
    }

    public void appendStatsTooltip(QuarryBlock quarryBlock, Consumer<Component> textConsumer) {
        float yield_bonus = (this.getFortuneMultiplier() - 1) * 100.0F;
        float energy_usage = quarryBlock.getBaseConsumption() * this.getInefficiency();
        float operations = (20.0F / quarryBlock.getTicksPerOperation()) * this.getSpeedMultiplier();

        SupplicableAlt<?> HasShiftDown = SupplicableAlt.when(Minecraft.getInstance().hasShiftDown());

        SupplicableAlt<String> PER_TYPE = HasShiftDown.<String>copy()
                .then(() -> "per_operation")
                .or(() -> "per_tick");
        SupplicableAlt<String> PER_UNIT = HasShiftDown.<String>copy()
                .then(() -> ZUtil.expandableFixedInt((int) (energy_usage * 20 / operations)))
                .or(() -> ReadableString.cIntFrom(energy_usage));

        MutableComponent LORE_USAGE_DETAIL = ReadableString.translatable("item.iron_quarry.lore.energy." + PER_TYPE.get(), PER_UNIT.get());
        MutableComponent LORE_USAGE = ReadableString.translatable("item.iron_quarry.quarry_block.lore.usage", LORE_USAGE_DETAIL);

        MutableComponent LORE_SPEED_DETAIL = ReadableString.translatable("item.iron_quarry.lore.operation.per_second", ZUtil.expandableFixedFloat(operations));
        MutableComponent LORE_SPEED = ReadableString.translatable("item.iron_quarry.quarry_block.lore.speed", LORE_SPEED_DETAIL);

        MutableComponent LORE_YIELD_DETAIL = ReadableString.translatable("item.iron_quarry.lore.percentage.positive", ZUtil.expandableFixedFloat(yield_bonus));
        MutableComponent LORE_YIELD = ReadableString.translatable("item.iron_quarry.quarry_block.lore.yield", LORE_YIELD_DETAIL);

        textConsumer.accept(LORE_USAGE);
        textConsumer.accept(LORE_SPEED);
        textConsumer.accept(LORE_YIELD);
    }
    public void appendConflictTooltip(Consumer<Component> textConsumer) {
        int conflictFlag = this.getConflictFlag();

        if(hasConflict(conflictFlag, CONFLICT_SPEED)) {
            textConsumer.accept(ReadableString.translatable("item.iron_quarry.conflict.speed_limit", ZUtil.expandableFixedFloat((SPEED_LIMIT-1)*100)));
        }
        if(hasConflict(conflictFlag, CONFLICT_FORTUNE)) {
            textConsumer.accept(ReadableString.translatable("item.iron_quarry.conflict.fortune_limit", ZUtil.expandableFixedFloat((FORTUNE_LIMIT-1)*100)));
        }
        if(hasConflict(conflictFlag, CONFLICT_SILK_TOUCH)) {
            textConsumer.accept(ReadableString.translatable("item.iron_quarry.conflict.silk_touch_fortune"));
        }
    }
}
