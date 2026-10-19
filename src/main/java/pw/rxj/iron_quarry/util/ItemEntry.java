package pw.rxj.iron_quarry.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import pw.rxj.iron_quarry.Main;

import java.util.function.Function;

public class ItemEntry<T extends Item> implements ItemConvertible {
    private final T item;
    private final Identifier id;

    private ItemEntry(Identifier id, T item) {
        this.id = id;
        this.item = item;
    }

    public static <T extends Item> ItemEntry<T> of(String path, Function<Item.Settings, T> factory) {
        Identifier id = Identifier.of(Main.MOD_ID, path);
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);

        return new ItemEntry<>(id, factory.apply(new Item.Settings().registryKey(key)));
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
