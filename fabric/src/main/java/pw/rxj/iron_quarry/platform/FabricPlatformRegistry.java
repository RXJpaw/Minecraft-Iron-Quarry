package pw.rxj.iron_quarry.platform;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;

import java.util.function.Supplier;

public class FabricPlatformRegistry implements IPlatformRegistry {
    @Override
    @SuppressWarnings("unchecked")
    public <T> Supplier<T> register(ResourceKey<? extends Registry<? super T>> registryKey, Identifier id, Supplier<T> factory) {
        Registry<T> registry = (Registry<T>) BuiltInRegistries.REGISTRY.getValue(registryKey.identifier());
        T value = Registry.register(registry, id, factory.get());

        return () -> value;
    }

    @Override
    public CreativeModeTab.Builder createCreativeTabBuilder() {
        return FabricCreativeModeTab.builder();
    }
}
