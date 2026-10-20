package pw.rxj.iron_quarry.blockentity;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntityType;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.block.ZBlocks;

public class ZBlockEntities {
    public static final BlockEntityType<QuarryBlockEntity> QUARRY_BLOCK_ENTITY = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(Main.MOD_ID, "quarry_block_entity"),
            FabricBlockEntityTypeBuilder.create(QuarryBlockEntity::new,
                    ZBlocks.COPPER_QUARRY.getBlock(),
                    ZBlocks.IRON_QUARRY.getBlock(),
                    ZBlocks.GOLD_QUARRY.getBlock(),
                    ZBlocks.DIAMOND_QUARRY.getBlock(),
                    ZBlocks.NETHERITE_QUARRY.getBlock(),
                    ZBlocks.NETHER_STAR_QUARRY.getBlock()
            ).build()
    );

    public static void register(){

    }
}
