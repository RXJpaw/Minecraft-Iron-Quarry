package pw.rxj.iron_quarry.util;

import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.platform.Platform;

import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

public class ItemEntry<T extends Item> implements ItemLike {
    private final Identifier id;
    private final Function<Item.Properties, T> factory;
    private Supplier<T> item;

    private ItemEntry(Identifier id, Function<Item.Properties, T> factory) {
        this.id = id;
        this.factory = factory;
    }

    public static <T extends Item> ItemEntry<T> of(String path, Function<Item.Properties, T> factory) {
        Identifier id = Identifier.fromNamespaceAndPath(Main.MOD_ID, path);

        return new ItemEntry<>(id, factory);
    }

    public void register() {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, this.id);

        this.item = Platform.REGISTRY.register(Registries.ITEM, this.id, () -> this.factory.apply(new Item.Properties().setId(key)));
    }

    @Override
    public T asItem() {
        return item.get();
    }
    public T getItem() { return item.get(); }
    public Identifier getId() {
        return id;
    }
}
