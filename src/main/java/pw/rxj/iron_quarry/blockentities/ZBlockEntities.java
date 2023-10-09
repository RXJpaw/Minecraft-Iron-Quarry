package pw.rxj.iron_quarry.blockentities;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.blocks.ZBlocks;

public class ZBlockEntities {
    public static final BlockEntityType<QuarryBlockEntity> QUARRY_BLOCK_ENTITY = Registry.register(
            Registry.BLOCK_ENTITY_TYPE,
            new Identifier(Main.MOD_ID, "quarry_block_entity"),
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
