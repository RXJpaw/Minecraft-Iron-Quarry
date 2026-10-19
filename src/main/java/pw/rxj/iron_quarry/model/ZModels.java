package pw.rxj.iron_quarry.model;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.render.item.model.ItemModelTypes;
import net.minecraft.client.render.item.property.numeric.NumericProperties;
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
        ModelLoadingPlugin.register(pluginContext -> pluginContext.modifyModelOnLoad().register((model, context) -> {
            for (ComplexModel complexModel : complexModelList) {
                if(complexModel.isOf(context.id())) return complexModel;
            }

            return model;
        }));

        ItemModelTypes.ID_MAPPER.put(QuarryItemModel.ID, QuarryItemModel.Unbaked.CODEC);
        NumericProperties.ID_MAPPER.put(HandledModelPredicateProperty.ID, HandledModelPredicateProperty.CODEC);
    }
}
