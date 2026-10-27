package pw.rxj.iron_quarry.mixin;


import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pw.rxj.iron_quarry.event.GameLifecycleCallback;
import pw.rxj.iron_quarry.platform.Platform;

@Mixin(MinecraftServer.class)
public abstract class BootstrapServerMixin {
    @Inject(method="runServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;initServer()Z", shift = At.Shift.BEFORE))
    private void runServer(CallbackInfo ci) {
        if (!Platform.LOADER.isDedicatedServer()) return;

        GameLifecycleCallback.IMMINENT_FIRST_RELOAD.invoker().onImminentFirstReload();
        GameLifecycleCallback.IMMINENT_REGISTRY_FREEZE.invoker().onImminentRegistryFreeze();
    }
}
