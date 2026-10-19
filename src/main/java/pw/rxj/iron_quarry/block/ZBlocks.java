package pw.rxj.iron_quarry.block;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Rarity;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.resource.config.server.QuarryStatsConfig;
import pw.rxj.iron_quarry.util.BlockEntry;

import java.util.List;

public class ZBlocks {
    private static final QuarryStatsConfig.Handler QUARRY_STATS_CONFIG = Main.CONFIG.getQuarryStatsConfig();

    public static final BlockEntry<QuarryBlock> COPPER_QUARRY = BlockEntry.of("copper_quarry", AbstractBlock.Settings.copy(Blocks.COPPER_BLOCK), settings -> new QuarryBlock(settings, "block/copper_quarry", QUARRY_STATS_CONFIG.getCopperQuarry()));
    public static final BlockEntry<QuarryBlock> IRON_QUARRY = BlockEntry.of("iron_quarry", AbstractBlock.Settings.copy(Blocks.IRON_BLOCK), settings -> new QuarryBlock(settings, "block/iron_quarry", QUARRY_STATS_CONFIG.getIronQuarry()));
    public static final BlockEntry<QuarryBlock> GOLD_QUARRY = BlockEntry.of("gold_quarry", AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK), settings -> new QuarryBlock(settings, "block/gold_quarry", QUARRY_STATS_CONFIG.getGoldQuarry()));
    public static final BlockEntry<QuarryBlock> DIAMOND_QUARRY = BlockEntry.of("diamond_quarry", AbstractBlock.Settings.copy(Blocks.DIAMOND_BLOCK), settings -> new QuarryBlock(settings, "block/diamond_quarry", QUARRY_STATS_CONFIG.getDiamondQuarry()));
    public static final BlockEntry<QuarryBlock> NETHERITE_QUARRY = BlockEntry.of("netherite_quarry", AbstractBlock.Settings.copy(Blocks.NETHERITE_BLOCK), settings -> new QuarryBlock(settings, "block/netherite_quarry", QUARRY_STATS_CONFIG.getNetheriteQuarry()), settings -> settings.rarity(Rarity.UNCOMMON).fireproof());
    public static final BlockEntry<QuarryBlock> NETHER_STAR_QUARRY = BlockEntry.of("nether_star_quarry", AbstractBlock.Settings.copy(Blocks.NETHERITE_BLOCK), settings -> new QuarryBlock(settings, "block/nether_star_quarry", QUARRY_STATS_CONFIG.getNetherStarQuarry()), settings -> settings.rarity(Rarity.RARE).fireproof());
    public static final List<QuarryBlock> quarryBlockList = List.of(
            COPPER_QUARRY.getBlock(),
            IRON_QUARRY.getBlock(),
            GOLD_QUARRY.getBlock(),
            DIAMOND_QUARRY.getBlock(),
            NETHERITE_QUARRY.getBlock(),
            NETHER_STAR_QUARRY.getBlock()
    );

    public static final BlockEntry<RedstoneBlock> REINFORCED_REDSTONE_BLOCK = BlockEntry.of("reinforced_redstone_block", AbstractBlock.Settings.copy(Blocks.REDSTONE_BLOCK), RedstoneBlock::new);
    public static final BlockEntry<Block> REINFORCED_LAPIS_BLOCK = BlockEntry.of("reinforced_lapis_block", AbstractBlock.Settings.copy(Blocks.LAPIS_BLOCK), Block::new);

    private static final List<BlockEntry<? extends Block>> blockEntryList = List.of(
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
        for (BlockEntry<? extends Block> blockEntry : blockEntryList) {
            Registry.register(Registries.BLOCK, blockEntry.getId(), blockEntry.getBlock());
            Registry.register(Registries.ITEM, blockEntry.getId(), blockEntry.getBlockItem());
        }

        ItemGroupEvents.modifyEntriesEvent(Main.ITEM_GROUP).register(entries -> {
            blockEntryList.forEach(blockItemBlockEntry -> entries.add(blockItemBlockEntry.getBlock()));
        });
    }
}
