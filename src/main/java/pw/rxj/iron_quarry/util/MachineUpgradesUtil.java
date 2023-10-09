package pw.rxj.iron_quarry.util;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.items.AugmentItem;
import pw.rxj.iron_quarry.types.AugmentType;

public class MachineUpgradesUtil {
    private float fortuneMultiplier = 1;
    private float speedMultiplier = 1;
    private float inefficiency = 1;

    public MachineUpgradesUtil(Inventory inventory){
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);

            if (!(stack.getItem() instanceof AugmentItem augmentItem)) continue;

            AugmentType augmentType = augmentItem.getType(stack);

            float multiplier = augmentItem.getAmount(stack) * augmentType.getMultiplier();
            float inefficiency = augmentItem.getAmount(stack) * augmentType.getInefficiency();

            switch (augmentType) {
                case FORTUNE -> fortuneMultiplier += multiplier/100;
                case SPEED -> speedMultiplier += multiplier/100;
            }

            this.inefficiency += inefficiency/100;
        }
    }

    public float getFortuneMultiplier() {
        return fortuneMultiplier;
    }
    public float getSpeedMultiplier() {
        return speedMultiplier;
    }
    public float getInefficiency() {
        return inefficiency;
    }
}
