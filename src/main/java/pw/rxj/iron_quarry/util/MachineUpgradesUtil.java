package pw.rxj.iron_quarry.util;

import pw.rxj.iron_quarry.item.AugmentItem;
import pw.rxj.iron_quarry.item.DrillItem;
import pw.rxj.iron_quarry.types.AugmentType;

import java.util.List;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;

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

    public boolean exceedsSpeedLimit() {
        return this.speedMultiplier > SPEED_LIMIT;
    }
    public boolean exceedsFortuneLimit() {
        return this.fortuneMultiplier > FORTUNE_LIMIT;
    }
    public boolean conflictsWithSilkTouch() {
        return this.hasSilkTouch && this.fortuneMultiplier > 1;
    }
}
