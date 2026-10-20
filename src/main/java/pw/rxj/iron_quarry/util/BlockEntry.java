package pw.rxj.iron_quarry.util;

import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.interfaces.IBlockTooltipAppender;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class BlockEntry<T extends Block> implements ItemLike {
    private final T block;
    private final BlockItem blockItem;
    private final Identifier id;

    private BlockEntry(Identifier id, T block, BlockItem blockItem) {
        this.id = id;
        this.block = block;
        this.blockItem = blockItem;
    }

    public static <T extends Block> BlockEntry<T> of(String path, BlockBehaviour.Properties blockSettings, Function<BlockBehaviour.Properties, T> blockFactory, UnaryOperator<Item.Properties> itemSettings) {
        Identifier id = Identifier.fromNamespaceAndPath(Main.MOD_ID, path);

        T block = blockFactory.apply(blockSettings.setId(ResourceKey.create(Registries.BLOCK, id)));
        BlockItem blockItem = new BlockItem(block, itemSettings.apply(new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM, id))
                .useBlockDescriptionPrefix())) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> textConsumer, TooltipFlag type) {
                super.appendHoverText(stack, context, displayComponent, textConsumer, type);

                if(this.getBlock() instanceof IBlockTooltipAppender tooltipAppender) {
                    tooltipAppender.appendTooltip(stack, context, textConsumer, type);
                }
            }
        };

        return new BlockEntry<>(id, block, blockItem);
    }
    public static <T extends Block> BlockEntry<T> of(String path, BlockBehaviour.Properties blockSettings, Function<BlockBehaviour.Properties, T> blockFactory) {
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
