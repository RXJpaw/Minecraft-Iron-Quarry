package pw.rxj.iron_quarry.block;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneBlock;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.util.BlockEntryBuilder;

import java.util.List;

public class ZBlocks {
    public static final BlockEntryBuilder COPPER_QUARRY = new BlockEntryBuilder()
            .setBlock(new QuarryBlock(Blocks.COPPER_BLOCK, "block/copper_quarry",0, 40, 6_000, 16)) //640 RF ~ 50% Coal Generator
            .setItemSettings(new FabricItemSettings().group(Main.ITEM_GROUP))
            .setIdentifier(Main.MOD_ID, "copper_quarry");
    public static final BlockEntryBuilder IRON_QUARRY = new BlockEntryBuilder()
            .setBlock(new QuarryBlock(Blocks.IRON_BLOCK, "block/iron_quarry", 1, 30, 40_000, 48)) //1.440 RF ~ 12 Advanced Solar Panels (day/night average)
            .setItemSettings(new FabricItemSettings().group(Main.ITEM_GROUP))
            .setIdentifier(Main.MOD_ID, "iron_quarry");
    public static final BlockEntryBuilder GOLD_QUARRY = new BlockEntryBuilder()
            .setBlock(new QuarryBlock(Blocks.GOLD_BLOCK, "block/gold_quarry", 2, 20, 260_000, 160)) //3.200 RF ~ 16 Industrial Solar Panels (day/night average)
            .setItemSettings(new FabricItemSettings().group(Main.ITEM_GROUP))
            .setIdentifier(Main.MOD_ID, "gold_quarry");
    public static final BlockEntryBuilder DIAMOND_QUARRY = new BlockEntryBuilder()
            .setBlock(new QuarryBlock(Blocks.DIAMOND_BLOCK, "block/diamond_quarry", 3, 10, 1_500_000, 640)) //6.400 RF ~ 32 Ultimate Solar Panels (day/night average)
            .setItemSettings(new FabricItemSettings().group(Main.ITEM_GROUP))
            .setIdentifier(Main.MOD_ID, "diamond_quarry");
    public static final BlockEntryBuilder NETHERITE_QUARRY = new BlockEntryBuilder()
            .setBlock(new QuarryBlock(Blocks.NETHERITE_BLOCK, "block/netherite_quarry", 4, 5, 7_000_000, 2_500)) //12.500 RF ~ Nitro Reactor with Packed Ice
            .setItemSettings(new FabricItemSettings().group(Main.ITEM_GROUP).fireproof())
            .setIdentifier(Main.MOD_ID, "netherite_quarry");
    public static final BlockEntryBuilder NETHER_STAR_QUARRY = new BlockEntryBuilder()
            .setBlock(new QuarryBlock(Blocks.NETHERITE_BLOCK, "block/nether_star_quarry", 6, 2, 50_000_000, 12_300)) //24.600 RF ~ Nitro Reactor with Blue Ice
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