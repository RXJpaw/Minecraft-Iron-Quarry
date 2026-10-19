package pw.rxj.iron_quarry.model;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import pw.rxj.iron_quarry.block.ZBlocks;

import java.util.List;

public class ZModels {
    public static final ComplexModel COPPER_QUARRY_MODEL = QuarryModel.of(ZBlocks.COPPER_QUARRY.getBlock());
    public static final ComplexModel IRON_QUARRY_MODEL = QuarryModel.of(ZBlocks.IRON_QUARRY.getBlock());
    public static final ComplexModel GOLD_QUARRY_MODEL = QuarryModel.of(ZBlocks.GOLD_QUARRY.getBlock());
    public static final ComplexModel DIAMOND_QUARRY_MODEL = QuarryModel.of(ZBlocks.DIAMOND_QUARRY.getBlock());
    public static final ComplexModel NETHERITE_QUARRY_MODEL = QuarryModel.of(ZBlocks.NETHERITE_QUARRY.getBlock());
    public static final ComplexModel NETHER_STAR_QUARRY_MODEL = QuarryModel.of(ZBlocks.NETHER_STAR_QUARRY.getBlock());

    private static final List<ComplexModel> complexModelList = List.of(
            COPPER_QUARRY_MODEL,
            IRON_QUARRY_MODEL,
            GOLD_QUARRY_MODEL,
            DIAMOND_QUARRY_MODEL,
            NETHERITE_QUARRY_MODEL,
            NETHER_STAR_QUARRY_MODEL
    );

    public static void register() {
        ModelLoadingPlugin.register(pluginContext -> pluginContext.resolveModel().register(context ->
                complexModelList.stream().filter(model -> model.isOf(context.id())).findFirst().orElse(null)
        ));
    }
}
