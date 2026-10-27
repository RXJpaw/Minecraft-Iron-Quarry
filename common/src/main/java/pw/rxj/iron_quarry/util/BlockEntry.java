package pw.rxj.iron_quarry.util;

import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.interfaces.IBlockTooltipAppender;
import pw.rxj.iron_quarry.platform.Platform;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
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
    private final Identifier id;
    private final BlockBehaviour.Properties blockSettings;
    private final Function<BlockBehaviour.Properties, T> blockFactory;
    private final UnaryOperator<Item.Properties> itemSettings;
    private Supplier<T> block;
    private Supplier<BlockItem> blockItem;

    private BlockEntry(Identifier id, BlockBehaviour.Properties blockSettings, Function<BlockBehaviour.Properties, T> blockFactory, UnaryOperator<Item.Properties> itemSettings) {
        this.id = id;
        this.blockSettings = blockSettings;
        this.blockFactory = blockFactory;
        this.itemSettings = itemSettings;
    }

    public static <T extends Block> BlockEntry<T> of(String path, BlockBehaviour.Properties blockSettings, Function<BlockBehaviour.Properties, T> blockFactory, UnaryOperator<Item.Properties> itemSettings) {
        Identifier id = Identifier.fromNamespaceAndPath(Main.MOD_ID, path);

        return new BlockEntry<>(id, blockSettings, blockFactory, itemSettings);
    }
    public static <T extends Block> BlockEntry<T> of(String path, BlockBehaviour.Properties blockSettings, Function<BlockBehaviour.Properties, T> blockFactory) {
        return of(path, blockSettings, blockFactory, UnaryOperator.identity());
    }

    public void register() {
        this.block = Platform.REGISTRY.register(Registries.BLOCK, this.id, () -> this.blockFactory.apply(this.blockSettings.setId(ResourceKey.create(Registries.BLOCK, this.id))));
        this.blockItem = Platform.REGISTRY.register(Registries.ITEM, this.id, this::createBlockItem);
    }
    private BlockItem createBlockItem() {
        return new BlockItem(this.block.get(), this.itemSettings.apply(new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM, this.id))
                .useBlockDescriptionPrefix())) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> textConsumer, TooltipFlag type) {
                super.appendHoverText(stack, context, displayComponent, textConsumer, type);

                if(this.getBlock() instanceof IBlockTooltipAppender tooltipAppender) {
                    tooltipAppender.appendTooltip(stack, context, textConsumer, type);
                }
            }
        };
    }

    public T getBlock() {
        return block.get();
    }
    @Override
    public BlockItem asItem() {
        return blockItem.get();
    }
    public BlockItem getBlockItem() {
        return blockItem.get();
    }
    public Identifier getId() {
        return id;
    }
}
