package pw.rxj.iron_quarry.items;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.registry.Registry;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.util.ItemEntryBuilder;

import java.util.List;

public class ZItems {
    public static final ItemEntryBuilder BLUEPRINT = new ItemEntryBuilder()
            .setItem(new BlueprintItem(new FabricItemSettings().group(Main.ITEM_GROUP).maxCount(1)))
            .setIdentifier(Main.MOD_ID, "blueprint");

    public static final ItemEntryBuilder CONDUCTIVE_AMETHYST = new ItemEntryBuilder()
            .setItem(new Item(new FabricItemSettings().group(Main.ITEM_GROUP).maxCount(64)))
            .setIdentifier(Main.MOD_ID, "conductive_amethyst");

    public static final ItemEntryBuilder AUGMENT = new ItemEntryBuilder()
            .setItem(new AugmentItem(new FabricItemSettings().group(Main.ITEM_GROUP).maxCount(1)))
            .setIdentifier(Main.MOD_ID, "augment");

    private static final List<ItemEntryBuilder> itemEntryList = List.of(
            BLUEPRINT,
            CONDUCTIVE_AMETHYST,
            AUGMENT
    );

    public static void register(){
        for (ItemEntryBuilder itemEntry : itemEntryList) {
            Registry.register(Registry.ITEM, itemEntry.getIdentifier(), itemEntry.getItem());
        }
    }
}
