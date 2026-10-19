package pw.rxj.iron_quarry.util;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import pw.rxj.iron_quarry.Main;

import java.util.function.Function;
import java.util.function.UnaryOperator;

public class BlockEntry<T extends Block> implements ItemConvertible {
    private final T block;
    private final BlockItem blockItem;
    private final Identifier id;

    private BlockEntry(Identifier id, T block, BlockItem blockItem) {
        this.id = id;
        this.block = block;
        this.blockItem = blockItem;
    }

    public static <T extends Block> BlockEntry<T> of(String path, AbstractBlock.Settings blockSettings, Function<AbstractBlock.Settings, T> blockFactory, UnaryOperator<Item.Settings> itemSettings) {
        Identifier id = Identifier.of(Main.MOD_ID, path);

        T block = blockFactory.apply(blockSettings.registryKey(RegistryKey.of(RegistryKeys.BLOCK, id)));
        BlockItem blockItem = new BlockItem(block, itemSettings.apply(new Item.Settings()
                .registryKey(RegistryKey.of(RegistryKeys.ITEM, id))
                .useBlockPrefixedTranslationKey()));

        return new BlockEntry<>(id, block, blockItem);
    }
    public static <T extends Block> BlockEntry<T> of(String path, AbstractBlock.Settings blockSettings, Function<AbstractBlock.Settings, T> blockFactory) {
        return of(path, blockSettings, blockFactory, UnaryOperator.identity());
    }

    public T getBlock() {
        return block;
    }
    @Override
    public BlockItem asItem() {
        return blockItem;
    }
    public BlockItem getBlockItem() {
        return blockItem;
    }
    public Identifier getId() {
        return id;
    }
}
