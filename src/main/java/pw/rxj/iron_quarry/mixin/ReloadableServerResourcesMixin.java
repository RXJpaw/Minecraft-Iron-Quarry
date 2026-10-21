package pw.rxj.iron_quarry.mixin;

import net.fabricmc.api.EnvType;
import net.minecraft.server.ReloadableServerResources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pw.rxj.iron_quarry.Main;

import java.util.concurrent.CompletableFuture;

@Mixin(ReloadableServerResources.class)
public abstract class ReloadableServerResourcesMixin {
    @Inject(method = "loadResources", at = @At(value = "HEAD"))
    private static void loadResources(CallbackInfoReturnable<CompletableFuture<ReloadableServerResources>> cir) {
        Main.CONFIG.read(EnvType.SERVER);
        Main.CONFIG.applyServerChanges();
    }
}
