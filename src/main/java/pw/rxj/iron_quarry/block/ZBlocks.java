package pw.rxj.iron_quarry.block;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PoweredBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.resource.config.server.QuarryStatsConfig;
import pw.rxj.iron_quarry.util.BlockEntry;

import java.util.List;

public class ZBlocks {
    private static final QuarryStatsConfig.Handler QUARRY_STATS_CONFIG = Main.CONFIG.getQuarryStatsConfig();

    public static final BlockEntry<QuarryBlock> COPPER_QUARRY = BlockEntry.of("copper_quarry", BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK), settings -> new QuarryBlock(settings, "block/copper_quarry", QUARRY_STATS_CONFIG.getCopperQuarry()));
    public static final BlockEntry<QuarryBlock> IRON_QUARRY = BlockEntry.of("iron_quarry", BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK), settings -> new QuarryBlock(settings, "block/iron_quarry", QUARRY_STATS_CONFIG.getIronQuarry()));
    public static final BlockEntry<QuarryBlock> GOLD_QUARRY = BlockEntry.of("gold_quarry", BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK), settings -> new QuarryBlock(settings, "block/gold_quarry", QUARRY_STATS_CONFIG.getGoldQuarry()));
    public static final BlockEntry<QuarryBlock> DIAMOND_QUARRY = BlockEntry.of("diamond_quarry", BlockBehaviour.Properties.ofFullCopy(Blocks.DIAMOND_BLOCK), settings -> new QuarryBlock(settings, "block/diamond_quarry", QUARRY_STATS_CONFIG.getDiamondQuarry()));
    public static final BlockEntry<QuarryBlock> NETHERITE_QUARRY = BlockEntry.of("netherite_quarry", BlockBehaviour.Properties.ofFullCopy(Blocks.NETHERITE_BLOCK), settings -> new QuarryBlock(settings, "block/netherite_quarry", QUARRY_STATS_CONFIG.getNetheriteQuarry()), settings -> settings.rarity(Rarity.UNCOMMON).fireResistant());
    public static final BlockEntry<QuarryBlock> NETHER_STAR_QUARRY = BlockEntry.of("nether_star_quarry", BlockBehaviour.Properties.ofFullCopy(Blocks.NETHERITE_BLOCK), settings -> new QuarryBlock(settings, "block/nether_star_quarry", QUARRY_STATS_CONFIG.getNetherStarQuarry()), settings -> settings.rarity(Rarity.RARE).fireResistant());
    public static final List<QuarryBlock> quarryBlockList = List.of(
            COPPER_QUARRY.getBlock(),
            IRON_QUARRY.getBlock(),
            GOLD_QUARRY.getBlock(),
            DIAMOND_QUARRY.getBlock(),
            NETHERITE_QUARRY.getBlock(),
            NETHER_STAR_QUARRY.getBlock()
    );

    public static final BlockEntry<PoweredBlock> REINFORCED_REDSTONE_BLOCK = BlockEntry.of("reinforced_redstone_block", BlockBehaviour.Properties.ofFullCopy(Blocks.REDSTONE_BLOCK), PoweredBlock::new);
    public static final BlockEntry<Block> REINFORCED_LAPIS_BLOCK = BlockEntry.of("reinforced_lapis_block", BlockBehaviour.Properties.ofFullCopy(Blocks.LAPIS_BLOCK), Block::new);

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
            Registry.register(BuiltInRegistries.BLOCK, blockEntry.getId(), blockEntry.getBlock());
            Registry.register(BuiltInRegistries.ITEM, blockEntry.getId(), blockEntry.getBlockItem());
        }

        CreativeModeTabEvents.modifyOutputEvent(Main.ITEM_GROUP).register(entries -> {
            blockEntryList.forEach(blockItemBlockEntry -> entries.accept(blockItemBlockEntry.getBlock()));
        });
    }
}
