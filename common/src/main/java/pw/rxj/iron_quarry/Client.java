package pw.rxj.iron_quarry;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.item.ItemStack;
import pw.rxj.iron_quarry.block.QuarryBlock;
import pw.rxj.iron_quarry.block.ZBlocks;
import pw.rxj.iron_quarry.compat.Compat;
import pw.rxj.iron_quarry.compat.iris.IrisClient;
import pw.rxj.iron_quarry.event.ClientInteractionCallback;
import pw.rxj.iron_quarry.event.GameLifecycleCallback;
import pw.rxj.iron_quarry.interfaces.IHandledMainHandAttack;
import pw.rxj.iron_quarry.model.HandledModelPredicateProperty;
import pw.rxj.iron_quarry.model.QuarryItemModel;
import pw.rxj.iron_quarry.network.ZNetwork;
import pw.rxj.iron_quarry.platform.Platform;
import pw.rxj.iron_quarry.render.BlueprintPreviewRenderer;
import pw.rxj.iron_quarry.render.QuarryMonitorOverlayRenderer;
import pw.rxj.iron_quarry.render.QuarryMonitorPreviewRenderer;
import pw.rxj.iron_quarry.resource.ResourceReloadListener;
import pw.rxj.iron_quarry.resource.config.ConfigSide;
import pw.rxj.iron_quarry.screen.ClientConfigScreen;
import pw.rxj.iron_quarry.screen.QuarryBlockScreen;
import pw.rxj.iron_quarry.types.IoState;
import pw.rxj.iron_quarry.util.ZUtil;

@Environment(EnvType.CLIENT)
public class Client {
    public static void init() {
        Main.CONFIG.read(ConfigSide.CLIENT);
        Main.CONFIG.registerClient();

        GameLifecycleCallback.IMMINENT_FIRST_RELOAD.register(() -> ZBlocks.getQuarryBlocks().forEach(QuarryBlock::initClient));
        ResourceReloadListener.include(IoState.getTextureId());

        ClientInteractionCallback.PRE_ATTACK.register((player, clickCount) -> {
            ItemStack stack = player.getMainHandItem();

            if(ZUtil.getBlockOrItem(stack) instanceof IHandledMainHandAttack handledMainHandAttack) {
                return handledMainHandAttack.handleMainHandAttack(player, stack, clickCount);
            }

            return false;
        });

        QuarryMonitorPreviewRenderer.register();
        BlueprintPreviewRenderer.register();
        if(Compat.IRIS_LOADED) IrisClient.register();
        QuarryMonitorOverlayRenderer.register();
        ResourceReloadListener.register();
        QuarryItemModel.register();
        HandledModelPredicateProperty.register();

        Platform.CLIENT.registerMenuScreen(Main.QUARRY_BLOCK_SCREEN_HANDLER, QuarryBlockScreen::new);
        Platform.CLIENT.registerConfigScreen(ClientConfigScreen::new);

        ZNetwork.registerClient();
    }
}
