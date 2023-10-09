package pw.rxj.iron_quarry;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.util.Identifier;
import pw.rxj.iron_quarry.blockentities.ZBlockEntities;
import pw.rxj.iron_quarry.interfaces.IModelPredicateProvider;
import pw.rxj.iron_quarry.renderer.BlueprintPreviewRenderer;
import pw.rxj.iron_quarry.renderer.QuarryBlockEntityRenderer;
import pw.rxj.iron_quarry.screen.QuarryBlockScreen;

@Environment(EnvType.CLIENT)
public class Client implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HandledScreens.register(Main.QUARRY_BLOCK_SCREEN_HANDLER, QuarryBlockScreen::new);

        BlockEntityRendererFactories.register(ZBlockEntities.QUARRY_BLOCK_ENTITY, QuarryBlockEntityRenderer::new);

        WorldRenderEvents.END.register(BlueprintPreviewRenderer::render);

        ModelPredicateProviderRegistry.register(new Identifier(Main.MOD_ID, "handled_model_predicate"), (stack, world, entity, seed) -> {
            if(stack.getItem() instanceof IModelPredicateProvider modelPredicateProvider) {
                return modelPredicateProvider.getModelPredicate(stack, world, entity, seed);
            }

            return 0.0F;
        });
    }
}
