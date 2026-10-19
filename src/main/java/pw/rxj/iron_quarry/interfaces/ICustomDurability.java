package pw.rxj.iron_quarry.interfaces;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.data.ZComponents;

public interface ICustomDurability {
    int getMaxCDamage();

    default boolean isCDamaged(ItemStack stack) {
        return this.getCDamage(stack) > 0;
    }
    default int getCDamage(ItemStack stack) {
        return stack.getOrDefault(ZComponents.CUSTOM_DAMAGE, 0);
    }
    default void setCDamage(ItemStack stack, int damage) {
        stack.set(ZComponents.CUSTOM_DAMAGE, Math.max(0, damage));
    }

    default int getCDurability(ItemStack stack) {
        return this.getMaxCDamage() - this.getCDamage(stack);
    }

    default boolean cdamage(ItemStack stack, int amount, @Nullable ServerWorld world, @Nullable ServerPlayerEntity player) {
        if (amount > 0 && world != null) {
            amount = EnchantmentHelper.getItemDamage(world, stack, amount);
            if (amount <= 0) {
                return false;
            }
        }

        int i = this.getCDamage(stack) + amount;
        this.setCDamage(stack, i);
        return i >= this.getMaxCDamage();
    }

    default void triggerBreakEventIfNeeded(ItemStack stack, Vec3d breakPos, World breakWorld) {
        if(this.getCDurability(stack) > 0) return;

        if(breakWorld.isClient()) {
            stack.setCount(0);
            return;
        }

        this.triggerBreakEvent(stack, breakPos, (ServerWorld) breakWorld);
    }

    default void triggerBreakEvent(ItemStack stack, Vec3d breakPos, ServerWorld breakWorld) {
        double x = breakPos.getX();
        double y = breakPos.getY();
        double z = breakPos.getZ();

        PlaySoundS2CPacket soundPacket = new PlaySoundS2CPacket(SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.BLOCKS, x, y, z, 1.0F, 1.0F, breakWorld.getSeed());
        breakWorld.getServer().getPlayerManager().sendToAround(null, x, y, z, 64.0, breakWorld.getRegistryKey(), soundPacket);

        stack.setCount(0);
    }

    default int getCItemBarStep(ItemStack stack) {
        return Math.round(13.0F - (float)this.getCDamage(stack) * 13.0F / (float)this.getMaxCDamage());
    }
    default int getCItemBarColor(ItemStack stack) {
        float f = Math.max(0.0F, (float)this.getCDurability(stack) / (float)this.getMaxCDamage());
        return MathHelper.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
    }
}
