package pw.rxj.iron_quarry.platform;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class NeoForgePlatformRegistry implements IPlatformRegistry {
    private static final Map<ResourceKey<? extends Registry<?>>, List<Runnable>> PENDING_ENTRIES = new HashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public <T> Supplier<T> register(ResourceKey<? extends Registry<? super T>> registryKey, Identifier id, Supplier<T> factory) {
        RegisteredValue<T> registeredValue = new RegisteredValue<>(id);

        PENDING_ENTRIES.computeIfAbsent(registryKey, key -> new ArrayList<>()).add(() -> {
            Registry<T> registry = (Registry<T>) BuiltInRegistries.REGISTRY.getValue(registryKey.identifier());

            registeredValue.value = Registry.register(registry, id, factory.get());
        });

        return registeredValue;
    }

    @Override
    public CreativeModeTab.Builder createCreativeTabBuilder() {
        return CreativeModeTab.builder();
    }

    public static void registerPendingEntries(RegisterEvent event) {
        List<Runnable> pendingEntries = PENDING_ENTRIES.remove(event.getRegistryKey());
        if(pendingEntries == null) return;

        pendingEntries.forEach(Runnable::run);
    }

    private static class RegisteredValue<T> implements Supplier<T> {
        private final Identifier id;
        private T value;

        private RegisteredValue(Identifier id) {
            this.id = id;
        }

        @Override
        public T get() {
            if(this.value == null) throw new IllegalStateException("Registry entry not registered yet: " + this.id);

            return this.value;
        }
    }
}
