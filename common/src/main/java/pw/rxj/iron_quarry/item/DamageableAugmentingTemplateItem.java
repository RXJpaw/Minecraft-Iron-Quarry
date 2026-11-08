package pw.rxj.iron_quarry.item;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import pw.rxj.iron_quarry.interfaces.ICustomDurability;
import pw.rxj.iron_quarry.util.ReadableString;
import pw.rxj.iron_quarry.util.ZUtil;

import java.util.function.Consumer;
import java.util.function.IntSupplier;

public class DamageableAugmentingTemplateItem extends AugmentingTemplateItem implements ICustomDurability {
    private final IntSupplier maxCDamage;

    private DamageableAugmentingTemplateItem(Item.Properties settings, IntSupplier maxDamage) {
        super(settings);

        this.maxCDamage = maxDamage;
    }
    public static DamageableAugmentingTemplateItem of(Item.Properties settings, IntSupplier maxDamage) {
        return new DamageableAugmentingTemplateItem(settings, maxDamage);
    }

    @Override
    public int getMaxCDamage() {
        return this.maxCDamage.getAsInt();
    }

    @Override
    public boolean canUse(ItemStack stack, int cost) {
        return this.getCDurability(stack) >= cost;
    }
    @Override
    public void use(ItemStack stack, int cost) {
        this.cdamage(stack, cost, null, null);
    }
    @Override
    public void breakIfUsedUp(ItemStack stack, Vec3 breakPos, Level breakWorld) {
        this.triggerBreakEventIfNeeded(stack, breakPos, breakWorld);
    }

    @Override
    protected void appendDurabilityText(ItemStack stack, Consumer<Component> textConsumer) {
        int maxDamage = this.getMaxCDamage();
        int durability = this.getCDurability(stack);
        String durabilityPercent = ZUtil.expandableFixedFloat((float) durability / maxDamage * 100);

        MutableComponent LORE_DURABILITY = ReadableString.translatable("item.iron_quarry.lore.custom_durability", ZUtil.expandableFixedInt(durability), ZUtil.expandableFixedInt(maxDamage), durabilityPercent);

        textConsumer.accept(LORE_DURABILITY);
    }
}
