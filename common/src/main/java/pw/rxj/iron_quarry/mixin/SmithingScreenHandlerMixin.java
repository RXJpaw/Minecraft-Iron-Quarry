package pw.rxj.iron_quarry.mixin;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pw.rxj.iron_quarry.interfaces.IHandledSmithing;
import pw.rxj.iron_quarry.util.ZUtil;


@Mixin(SmithingMenu.class)
public abstract class SmithingScreenHandlerMixin extends ItemCombinerMenu {
    public SmithingScreenHandlerMixin(@Nullable MenuType<?> type, int syncId, Inventory playerInventory, ContainerLevelAccess context, ItemCombinerMenuSlotDefinition forgingSlotsManager) {
        super(type, syncId, playerInventory, context, forgingSlotsManager);
    }

    @Inject(method="onTake", at = @At(value = "HEAD"), cancellable = true)
    private void onTakeOutput(Player player, ItemStack stack, CallbackInfo ci) {
        //TESTME: stack is "0 air" when taking output with shift (MC-267545)
        if(ZUtil.getBlockOrItem(this.inputSlots.getItem(1)) instanceof IHandledSmithing handledSmithing) {
            boolean result = handledSmithing.handleSmithingTakeOutput(player, this.inputSlots, this.resultSlots, this.access);
            if(result) ci.cancel();
        }
    }
}
