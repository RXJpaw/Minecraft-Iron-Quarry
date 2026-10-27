package pw.rxj.iron_quarry.platform;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;

import java.util.function.Supplier;

public interface IPlatformRegistry {
    <T> Supplier<T> register(ResourceKey<? extends Registry<? super T>> registryKey, Identifier id, Supplier<T> factory);
    CreativeModeTab.Builder createCreativeTabBuilder();
}
