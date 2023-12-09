package pw.rxj.iron_quarry.model;

import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelResourceProvider;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.block.QuarryBlock;
import pw.rxj.iron_quarry.block.ZBlocks;

import java.util.List;

public class ZModels implements ModelResourceProvider {
    public static final ComplexModel COPPER_QUARRY_MODEL = QuarryModel.of((QuarryBlock) ZBlocks.COPPER_QUARRY.getBlock());
    public static final ComplexModel IRON_QUARRY_MODEL = QuarryModel.of((QuarryBlock) ZBlocks.IRON_QUARRY.getBlock());
    public static final ComplexModel GOLD_QUARRY_MODEL = QuarryModel.of((QuarryBlock) ZBlocks.GOLD_QUARRY.getBlock());
    public static final ComplexModel DIAMOND_QUARRY_MODEL = QuarryModel.of((QuarryBlock) ZBlocks.DIAMOND_QUARRY.getBlock());
    public static final ComplexModel NETHERITE_QUARRY_MODEL = QuarryModel.of((QuarryBlock) ZBlocks.NETHERITE_QUARRY.getBlock());
    public static final ComplexModel NETHER_STAR_QUARRY_MODEL = QuarryModel.of((QuarryBlock) ZBlocks.NETHER_STAR_QUARRY.getBlock());

    private static final List<ComplexModel> complexModelList = List.of(
            COPPER_QUARRY_MODEL,
            IRON_QUARRY_MODEL,
            GOLD_QUARRY_MODEL,
            DIAMOND_QUARRY_MODEL,
            NETHERITE_QUARRY_MODEL,
            NETHER_STAR_QUARRY_MODEL
    );

    private ResourceManager resourceManager;

    private ZModels(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    @Override
    public @Nullable UnbakedModel loadModelResource(Identifier resourceId, ModelProviderContext context) {
        return complexModelList.stream().filter(model -> model.isOf(resourceId)).findFirst().orElse(null);
    }

    public static void register() {
        ModelLoadingRegistry.INSTANCE.registerResourceProvider(ZModels::new);
    }
}
