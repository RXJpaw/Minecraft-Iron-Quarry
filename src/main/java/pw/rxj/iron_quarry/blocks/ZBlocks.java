package pw.rxj.iron_quarry.blocks;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneBlock;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.util.BlockEntryBuilder;

import java.util.List;

public class ZBlocks {
    public static final BlockEntryBuilder COPPER_QUARRY = new BlockEntryBuilder()
            .setBlock(new QuarryBlock(Blocks.COPPER_BLOCK, "block/copper_quarry",6, 40, 12_000, 48))
            .setItemSettings(new FabricItemSettings().group(Main.ITEM_GROUP))
            .setIdentifier(Main.MOD_ID, "copper_quarry");
    public static final BlockEntryBuilder IRON_QUARRY = new BlockEntryBuilder()
            .setBlock(new QuarryBlock(Blocks.IRON_BLOCK, "block/iron_quarry", 6, 30, 40_000, 96))
            .setItemSettings(new FabricItemSettings().group(Main.ITEM_GROUP))
            .setIdentifier(Main.MOD_ID, "iron_quarry");
    public static final BlockEntryBuilder GOLD_QUARRY = new BlockEntryBuilder()
            .setBlock(new QuarryBlock(Blocks.GOLD_BLOCK, "block/gold_quarry", 6, 20, 135_000, 216))
            .setItemSettings(new FabricItemSettings().group(Main.ITEM_GROUP))
            .setIdentifier(Main.MOD_ID, "gold_quarry");
    public static final BlockEntryBuilder DIAMOND_QUARRY = new BlockEntryBuilder()
            .setBlock(new QuarryBlock(Blocks.DIAMOND_BLOCK, "block/diamond_quarry", 6, 10, 450_000, 648))
            .setItemSettings(new FabricItemSettings().group(Main.ITEM_GROUP))
            .setIdentifier(Main.MOD_ID, "diamond_quarry");
    public static final BlockEntryBuilder NETHERITE_QUARRY = new BlockEntryBuilder()
            .setBlock(new QuarryBlock(Blocks.NETHERITE_BLOCK, "block/netherite_quarry", 6, 5, 1_500_000, 1_944))
            .setItemSettings(new FabricItemSettings().group(Main.ITEM_GROUP).fireproof())
            .setIdentifier(Main.MOD_ID, "netherite_quarry");
    public static final BlockEntryBuilder NETHER_STAR_QUARRY = new BlockEntryBuilder()
            .setBlock(new QuarryBlock(Blocks.NETHERITE_BLOCK, "block/nether_star_quarry", 6, 2, 5_000_000, 7_290))
            .setItemSettings(new FabricItemSettings().group(Main.ITEM_GROUP).rarity(Rarity.UNCOMMON).fireproof())
            .setIdentifier(Main.MOD_ID, "nether_star_quarry");
    public static final BlockEntryBuilder REINFORCED_REDSTONE_BLOCK = new BlockEntryBuilder()
            .setBlock(new RedstoneBlock(AbstractBlock.Settings.copy(Blocks.REDSTONE_BLOCK)))
            .setItemSettings(new FabricItemSettings().group(Main.ITEM_GROUP))
            .setIdentifier(Main.MOD_ID, "reinforced_redstone_block");
    public static final BlockEntryBuilder REINFORCED_LAPIS_BLOCK = new BlockEntryBuilder()
            .setBlock(new Block(AbstractBlock.Settings.copy(Blocks.LAPIS_BLOCK)))
            .setItemSettings(new FabricItemSettings().group(Main.ITEM_GROUP))
            .setIdentifier(Main.MOD_ID, "reinforced_lapis_block");

    private static final List<BlockEntryBuilder> blockEntryList = List.of(
            COPPER_QUARRY,
            IRON_QUARRY,
            GOLD_QUARRY,
            DIAMOND_QUARRY,
            NETHERITE_QUARRY,
            NETHER_STAR_QUARRY,
            REINFORCED_REDSTONE_BLOCK,
            REINFORCED_LAPIS_BLOCK
    );

    public static void register(){
        for (BlockEntryBuilder blockEntry : blockEntryList) {
            Registry.register(Registry.BLOCK, blockEntry.getIdentifier(), blockEntry.getBlock());
            Registry.register(Registry.ITEM, blockEntry.getIdentifier(), blockEntry.getBlockItem());

            if(FabricLoader.getInstance().getEnvironmentType().equals(EnvType.SERVER)) continue;

            BlockRenderLayerMap.INSTANCE.putBlock(blockEntry.getBlock(), RenderLayer.getCutoutMipped());
        }
    }
}
