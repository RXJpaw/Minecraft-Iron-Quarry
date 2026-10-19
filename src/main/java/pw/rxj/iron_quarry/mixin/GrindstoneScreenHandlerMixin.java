package pw.rxj.iron_quarry.mixin;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pw.rxj.iron_quarry.interfaces.IHandledGrinding;
import pw.rxj.iron_quarry.util.ZUtil;

@Mixin(GrindstoneScreenHandler.class)
public abstract class GrindstoneScreenHandlerMixin extends ScreenHandler {
    protected GrindstoneScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    @Shadow @Final
    Inventory input;

    @Shadow @Final
    private Inventory result;

    @Inject(method = "updateResult", at = @At(value = "HEAD"), cancellable = true)
    private void updateResult(CallbackInfo ci) {
        ItemStack input0 = this.input.getStack(0);
        ItemStack input1 = this.input.getStack(1);

        boolean input0handled = ZUtil.getBlockOrItem(input0) instanceof IHandledGrinding handledGrinding && handledGrinding.isGrindable(input0);
        boolean input1handled = ZUtil.getBlockOrItem(input1) instanceof IHandledGrinding handledGrinding && handledGrinding.isGrindable(input1);
        if(!input0handled && !input1handled) return;
        if(input0handled && input1handled) return;

        ItemStack stack = input0handled ? input0 : input1;
        ItemStack follower = input0handled ? input1 : input0;
        IHandledGrinding handledGrinding = (IHandledGrinding) stack.getItem();

        ItemStack output = handledGrinding.getGrindingOutput(stack, follower);
        this.result.setStack(0, output == null ? ItemStack.EMPTY : output);
        this.sendContentUpdates();
        ci.cancel();
    }
}
