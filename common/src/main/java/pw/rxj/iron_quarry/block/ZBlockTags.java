package pw.rxj.iron_quarry.block;

import pw.rxj.iron_quarry.Main;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class ZBlockTags {
    private static TagKey<Block> of(String path) {
        return TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(Main.MOD_ID, path));
    }

    public static final TagKey<Block> IGNORED_BY_QUARRY = of("ignored_by_quarry");
    public static final TagKey<Block> MODIFIABLE_WITHOUT_BREAK_CHECK = of("modifiable_without_break_check");
    public static final TagKey<Block> MINED_DESPITE_BLOCK_ENTITY = of("mined_despite_block_entity");
}
