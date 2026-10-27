package pw.rxj.iron_quarry;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.item.ItemStack;
import pw.rxj.iron_quarry.block.QuarryBlock;
import pw.rxj.iron_quarry.block.ZBlocks;
import pw.rxj.iron_quarry.compat.Compat;
import pw.rxj.iron_quarry.compat.iris.IrisClient;
import pw.rxj.iron_quarry.interfaces.IHandledMainHandAttack;
import pw.rxj.iron_quarry.model.ZModels;
import pw.rxj.iron_quarry.network.ZNetwork;
import pw.rxj.iron_quarry.render.BlueprintPreviewRenderer;
import pw.rxj.iron_quarry.render.QuarryMonitorOverlayRenderer;
import pw.rxj.iron_quarry.render.QuarryMonitorPreviewRenderer;
import pw.rxj.iron_quarry.resource.ResourceReloadListener;
import pw.rxj.iron_quarry.screen.QuarryBlockScreen;
import pw.rxj.iron_quarry.types.IoState;
import pw.rxj.iron_quarry.util.ZUtil;

@Environment(EnvType.CLIENT)
public class Client implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Main.CONFIG.read(EnvType.CLIENT);
        Main.CONFIG.registerClient();

        ZBlocks.quarryBlockList.forEach(QuarryBlock::initClient);
        ResourceReloadListener.include(IoState.getTextureId());

        ClientPreAttackCallback.EVENT.register((client, player, clickCount) -> {
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
        ZModels.register();

        MenuScreens.register(Main.QUARRY_BLOCK_SCREEN_HANDLER, QuarryBlockScreen::new);

        ZNetwork.registerClient();
    }
}
