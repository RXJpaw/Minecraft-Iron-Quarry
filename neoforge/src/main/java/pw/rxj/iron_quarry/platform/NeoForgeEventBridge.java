package pw.rxj.iron_quarry.platform;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import pw.rxj.iron_quarry.event.*;

public class NeoForgeEventBridge {
    public static void register() {
        NeoForge.EVENT_BUS.addListener((ServerStartingEvent event) -> ServerLifecycleCallback.STARTING.invoker().onStarting(event.getServer()));

        NeoForge.EVENT_BUS.addListener((LevelEvent.Load event) -> {
            if(event.getLevel() instanceof ServerLevel serverWorld) ServerLevelCallback.LOAD.invoker().onLoad(serverWorld);
        });
        NeoForge.EVENT_BUS.addListener((LevelEvent.Unload event) -> {
            if(event.getLevel() instanceof ServerLevel serverWorld) ServerLevelCallback.UNLOAD.invoker().onUnload(serverWorld);
        });
        NeoForge.EVENT_BUS.addListener((LevelTickEvent.Pre event) -> {
            if(event.getLevel() instanceof ServerLevel serverWorld) ServerLevelCallback.START_TICK.invoker().onStartTick(serverWorld);
        });

        NeoForge.EVENT_BUS.addListener((EntityJoinLevelEvent event) -> {
            if(event.getLevel() instanceof ServerLevel serverWorld) ServerEntityCallback.LOAD.invoker().onLoad(event.getEntity(), serverWorld);
        });

        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> {
            if(event.getEntity() instanceof ServerPlayer player) ServerConnectionCallback.JOIN.invoker().onJoin(player);
        });

        NeoForge.EVENT_BUS.addListener((PlayerInteractEvent.LeftClickBlock event) -> {
            if(event.getAction() != PlayerInteractEvent.LeftClickBlock.Action.START) return;

            InteractionResult result = PlayerInteractionCallback.ATTACK_BLOCK.invoker().onAttackBlock(event.getEntity(), event.getLevel(), event.getHand(), event.getPos(), event.getFace());
            if(!result.equals(InteractionResult.PASS)) event.setCanceled(true);
        });
        NeoForge.EVENT_BUS.addListener((PlayerInteractEvent.RightClickBlock event) -> {
            InteractionResult result = PlayerInteractionCallback.USE_BLOCK.invoker().onUseBlock(event.getEntity(), event.getLevel(), event.getHand(), event.getHitVec());

            if(!result.equals(InteractionResult.PASS)) {
                event.setCanceled(true);
                event.setCancellationResult(result);
            }
        });

        NeoForge.EVENT_BUS.addListener((LootTableLoadEvent event) -> LootTableCallback.MODIFY.invoker().onModify(event.getName(), poolBuilder -> event.getTable().addPool(poolBuilder.build())));
    }

    public static void registerClient() {
        NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> ClientConnectionCallback.DISCONNECT.invoker().onDisconnect());
    }
}
