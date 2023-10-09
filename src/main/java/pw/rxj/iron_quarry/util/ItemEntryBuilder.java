package pw.rxj.iron_quarry.util;

import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

public class ItemEntryBuilder {
    private Item item;
    private Identifier identifier;

    public ItemEntryBuilder setItem(Item item) {
        this.item = item;
        return this;
    }

    public ItemEntryBuilder setIdentifier(String namespace, String path) {
        this.identifier = new Identifier(namespace, path);
        return this;
    }

    public Item getItem() {
        return item;
    }
    public Identifier getIdentifier() {
        return identifier;
    }
}
