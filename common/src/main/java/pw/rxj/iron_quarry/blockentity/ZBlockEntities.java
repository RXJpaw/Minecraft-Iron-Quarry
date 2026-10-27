package pw.rxj.iron_quarry.blockentity;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntityType;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.block.ZBlocks;
import pw.rxj.iron_quarry.platform.Platform;

import java.util.Set;

public class ZBlockEntities {
    public static BlockEntityType<QuarryBlockEntity> QUARRY_BLOCK_ENTITY;

    public static void register(){
        Platform.REGISTRY.register(Registries.BLOCK_ENTITY_TYPE, Identifier.fromNamespaceAndPath(Main.MOD_ID, "quarry_block_entity"), () -> QUARRY_BLOCK_ENTITY = new BlockEntityType<>(QuarryBlockEntity::new, Set.of(
                ZBlocks.COPPER_QUARRY.getBlock(),
                ZBlocks.IRON_QUARRY.getBlock(),
                ZBlocks.GOLD_QUARRY.getBlock(),
                ZBlocks.DIAMOND_QUARRY.getBlock(),
                ZBlocks.NETHERITE_QUARRY.getBlock(),
                ZBlocks.NETHER_STAR_QUARRY.getBlock()
        )));

        Platform.ENERGY.registerBlockEntity(() -> QUARRY_BLOCK_ENTITY, quarryBlockEntity -> quarryBlockEntity.EnergyContainer);
        Platform.ITEM_TRANSFER.registerBlockEntity(() -> QUARRY_BLOCK_ENTITY, QuarryBlockEntity::getLinkedContainer);
    }
}
