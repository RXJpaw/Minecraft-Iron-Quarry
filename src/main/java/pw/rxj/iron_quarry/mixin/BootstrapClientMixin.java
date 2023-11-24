package pw.rxj.iron_quarry.mixin;


import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pw.rxj.iron_quarry.event.GameLifecycleCallback;

@Mixin(MinecraftClient.class)
public abstract class BootstrapClientMixin {
    @Inject(method = "run", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;thread:Ljava/lang/Thread;", ordinal = 0, shift = At.Shift.BEFORE))
    private void runClient(CallbackInfo ci) {
        GameLifecycleCallback.IMMINENT_REGISTRY_FREEZE.invoker().onImminentRegistryFreeze();
    }
}
