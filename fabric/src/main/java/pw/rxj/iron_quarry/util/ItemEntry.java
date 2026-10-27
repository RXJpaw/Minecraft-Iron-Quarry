package pw.rxj.iron_quarry.util;

import pw.rxj.iron_quarry.Main;

import java.util.function.Function;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

public class ItemEntry<T extends Item> implements ItemLike {
    private final T item;
    private final Identifier id;

    private ItemEntry(Identifier id, T item) {
        this.id = id;
        this.item = item;
    }

    public static <T extends Item> ItemEntry<T> of(String path, Function<Item.Properties, T> factory) {
        Identifier id = Identifier.fromNamespaceAndPath(Main.MOD_ID, path);
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);

        return new ItemEntry<>(id, factory.apply(new Item.Properties().setId(key)));
    }

    @Override
    public T asItem() {
        return item;
    }
    public T getItem() { return item; }
    public Identifier getId() {
        return id;
    }
}
