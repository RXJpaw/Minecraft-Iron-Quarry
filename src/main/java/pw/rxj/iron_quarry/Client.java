package pw.rxj.iron_quarry;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.util.Identifier;
import pw.rxj.iron_quarry.block.QuarryBlock;
import pw.rxj.iron_quarry.block.ZBlocks;
import pw.rxj.iron_quarry.interfaces.IModelPredicateProvider;
import pw.rxj.iron_quarry.model.ZModels;
import pw.rxj.iron_quarry.network.ZNetwork;
import pw.rxj.iron_quarry.render.BlueprintPreviewRenderer;
import pw.rxj.iron_quarry.resource.ResourceReloadListener;
import pw.rxj.iron_quarry.screen.QuarryBlockScreen;
import pw.rxj.iron_quarry.types.IoState;

@Environment(EnvType.CLIENT)
public class Client implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ZBlocks.quarryBlockList.forEach(QuarryBlock::initClient);
        ResourceReloadListener.include(IoState.getTextureId());

        BlueprintPreviewRenderer.register();
        ResourceReloadListener.register();
        ZModels.register();

        HandledScreens.register(Main.QUARRY_BLOCK_SCREEN_HANDLER, QuarryBlockScreen::new);

        ModelPredicateProviderRegistry.register(new Identifier(Main.MOD_ID, "handled_model_predicate"), (stack, world, entity, seed) -> {
            if(stack.getItem() instanceof IModelPredicateProvider modelPredicateProvider) {
                return modelPredicateProvider.getModelPredicate(stack, world, entity, seed);
            }

            return 0.0F;
        });

        ZNetwork.registerClient();
    }
}
