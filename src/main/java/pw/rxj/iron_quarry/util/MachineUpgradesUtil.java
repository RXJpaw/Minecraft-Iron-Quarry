package pw.rxj.iron_quarry.util;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import pw.rxj.iron_quarry.item.AugmentItem;
import pw.rxj.iron_quarry.types.AugmentType;

public class MachineUpgradesUtil {
    public static final float FORTUNE_LIMIT = 3.0F;
    public static final float SPEED_LIMIT = 91.0F;

    private boolean hasSilkTouch = false;

    private float fortuneMultiplier = 1;
    private float speedMultiplier = 1;
    private float inefficiency = 1;

    private MachineUpgradesUtil(Inventory inventory){
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);

            if (!(stack.getItem() instanceof AugmentItem augmentItem)) continue;

            AugmentType augmentType = augmentItem.getType(stack);

            float multiplier = augmentItem.getAmount(stack) * augmentType.getMultiplier();
            float inefficiency = augmentItem.getAmount(stack) * augmentType.getInefficiency();

            switch (augmentType) {
                case SPEED -> this.speedMultiplier += multiplier/100;
                case FORTUNE -> this.fortuneMultiplier += multiplier/100;
                case SILK_TOUCH -> this.hasSilkTouch = true;
            }

            this.inefficiency += inefficiency/100;
        }
    }

    public static MachineUpgradesUtil from(Inventory inventory) {
        return new MachineUpgradesUtil(inventory);
    }

    public boolean hasSilkTouch() { return this.hasSilkTouch; }

    public float getFortuneMultiplier() {
        return Math.min(fortuneMultiplier, FORTUNE_LIMIT);
    }
    public float getSpeedMultiplier() {
        return Math.min(speedMultiplier, SPEED_LIMIT);
    }
    public float getInefficiency() {
        return inefficiency;
    }
}
