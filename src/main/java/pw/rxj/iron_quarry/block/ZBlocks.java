package pw.rxj.iron_quarry.block;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneBlock;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.resource.ConfigHandler;
import pw.rxj.iron_quarry.util.BlockEntryBuilder;

import java.util.List;

public class ZBlocks {
    private static final ConfigHandler.QuarryStatsConfigHandler QuarryStatsConfig = Main.CONFIG.getQuarryStatsConfig();

    public static final BlockEntryBuilder COPPER_QUARRY = new BlockEntryBuilder()
            .setBlock(new QuarryBlock(Blocks.COPPER_BLOCK, "block/copper_quarry", QuarryStatsConfig.getCopperQuarry()))
            .setItemSettings(new FabricItemSettings().group(Main.ITEM_GROUP))
            .setIdentifier(Main.MOD_ID, "copper_quarry");
    public static final BlockEntryBuilder IRON_QUARRY = new BlockEntryBuilder()
            .setBlock(new QuarryBlock(Blocks.IRON_BLOCK, "block/iron_quarry", QuarryStatsConfig.getIronQuarry()))
            .setItemSettings(new FabricItemSettings().group(Main.ITEM_GROUP))
            .setIdentifier(Main.MOD_ID, "iron_quarry");
    public static final BlockEntryBuilder GOLD_QUARRY = new BlockEntryBuilder()
            .setBlock(new QuarryBlock(Blocks.GOLD_BLOCK, "block/gold_quarry", QuarryStatsConfig.getGoldQuarry()))
            .setItemSettings(new FabricItemSettings().group(Main.ITEM_GROUP))
            .setIdentifier(Main.MOD_ID, "gold_quarry");
    public static final BlockEntryBuilder DIAMOND_QUARRY = new BlockEntryBuilder()
            .setBlock(new QuarryBlock(Blocks.DIAMOND_BLOCK, "block/diamond_quarry", QuarryStatsConfig.getDiamondQuarry()))
            .setItemSettings(new FabricItemSettings().group(Main.ITEM_GROUP))
            .setIdentifier(Main.MOD_ID, "diamond_quarry");
    public static final BlockEntryBuilder NETHERITE_QUARRY = new BlockEntryBuilder()
            .setBlock(new QuarryBlock(Blocks.NETHERITE_BLOCK, "block/netherite_quarry", QuarryStatsConfig.getNetheriteQuarry()))
            .setItemSettings(new FabricItemSettings().group(Main.ITEM_GROUP).fireproof())
            .setIdentifier(Main.MOD_ID, "netherite_quarry");
    public static final BlockEntryBuilder NETHER_STAR_QUARRY = new BlockEntryBuilder()
            .setBlock(new QuarryBlock(Blocks.NETHERITE_BLOCK, "block/nether_star_quarry", QuarryStatsConfig.getNetherStarQuarry()))
            .setItemSettings(new FabricItemSettings().group(Main.ITEM_GROUP).rarity(Rarity.UNCOMMON).fireproof())
            .setIdentifier(Main.MOD_ID, "nether_star_quarry");
    public static final List<QuarryBlock> quarryBlockList = List.of(
            (QuarryBlock) COPPER_QUARRY.getBlock(),
            (QuarryBlock) IRON_QUARRY.getBlock(),
            (QuarryBlock) GOLD_QUARRY.getBlock(),
            (QuarryBlock) DIAMOND_QUARRY.getBlock(),
            (QuarryBlock) NETHERITE_QUARRY.getBlock(),
            (QuarryBlock) NETHER_STAR_QUARRY.getBlock()
    );

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
            Registry.register(Registry.BLOCK, blockEntry.getId(), blockEntry.getBlock());
            Registry.register(Registry.ITEM, blockEntry.getId(), blockEntry.getBlockItem());
        }
    }
}
