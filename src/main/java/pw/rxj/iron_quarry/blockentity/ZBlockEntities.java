package pw.rxj.iron_quarry.blockentity;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.block.ZBlocks;

public class ZBlockEntities {
    public static final BlockEntityType<QuarryBlockEntity> QUARRY_BLOCK_ENTITY = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            new Identifier(Main.MOD_ID, "quarry_block_entity"),
            BlockEntityType.Builder.create(QuarryBlockEntity::new,
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
