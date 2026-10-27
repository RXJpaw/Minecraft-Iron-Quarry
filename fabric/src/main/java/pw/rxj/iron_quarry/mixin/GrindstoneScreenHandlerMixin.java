package pw.rxj.iron_quarry.mixin;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pw.rxj.iron_quarry.interfaces.IHandledGrinding;
import pw.rxj.iron_quarry.util.ZUtil;

@Mixin(GrindstoneMenu.class)
public abstract class GrindstoneScreenHandlerMixin extends AbstractContainerMenu {
    protected GrindstoneScreenHandlerMixin(@Nullable MenuType<?> type, int syncId) {
        super(type, syncId);
    }

    @Shadow @Final
    Container repairSlots;

    @Shadow @Final
    private Container resultSlots;

    @Inject(method = "createResult", at = @At(value = "HEAD"), cancellable = true)
    private void updateResult(CallbackInfo ci) {
        ItemStack input0 = this.repairSlots.getItem(0);
        ItemStack input1 = this.repairSlots.getItem(1);

        boolean input0handled = ZUtil.getBlockOrItem(input0) instanceof IHandledGrinding handledGrinding && handledGrinding.isGrindable(input0);
        boolean input1handled = ZUtil.getBlockOrItem(input1) instanceof IHandledGrinding handledGrinding && handledGrinding.isGrindable(input1);
        if(!input0handled && !input1handled) return;
        if(input0handled && input1handled) return;

        ItemStack stack = input0handled ? input0 : input1;
        ItemStack follower = input0handled ? input1 : input0;
        IHandledGrinding handledGrinding = (IHandledGrinding) stack.getItem();

        ItemStack output = handledGrinding.getGrindingOutput(stack, follower);
        this.resultSlots.setItem(0, output == null ? ItemStack.EMPTY : output);
        this.broadcastChanges();
        ci.cancel();
    }
}
