package pw.rxj.iron_quarry.interfaces;

import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
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

    default boolean cdamage(ItemStack stack, int amount, @Nullable ServerLevel world, @Nullable ServerPlayer player) {
        if (amount > 0 && world != null) {
            amount = EnchantmentHelper.processDurabilityChange(world, stack, amount);
            if (amount <= 0) {
                return false;
            }
        }

        int i = this.getCDamage(stack) + amount;
        this.setCDamage(stack, i);
        return i >= this.getMaxCDamage();
    }

    default void triggerBreakEventIfNeeded(ItemStack stack, Vec3 breakPos, Level breakWorld) {
        if(this.getCDurability(stack) > 0) return;

        if(breakWorld.isClientSide()) {
            stack.setCount(0);
            return;
        }

        this.triggerBreakEvent(stack, breakPos, (ServerLevel) breakWorld);
    }

    default void triggerBreakEvent(ItemStack stack, Vec3 breakPos, ServerLevel breakWorld) {
        double x = breakPos.x();
        double y = breakPos.y();
        double z = breakPos.z();

        ClientboundSoundPacket soundPacket = new ClientboundSoundPacket(SoundEvents.ITEM_BREAK, SoundSource.BLOCKS, x, y, z, 1.0F, 1.0F, breakWorld.getSeed());
        breakWorld.getServer().getPlayerList().broadcast(null, x, y, z, 64.0, breakWorld.dimension(), soundPacket);

        stack.setCount(0);
    }

    default int getCItemBarStep(ItemStack stack) {
        return Math.round(13.0F - (float)this.getCDamage(stack) * 13.0F / (float)this.getMaxCDamage());
    }
    default int getCItemBarColor(ItemStack stack) {
        float f = Math.max(0.0F, (float)this.getCDurability(stack) / (float)this.getMaxCDamage());
        return Mth.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
    }
}
