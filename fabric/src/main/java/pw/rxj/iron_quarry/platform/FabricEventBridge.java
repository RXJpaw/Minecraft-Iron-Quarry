package pw.rxj.iron_quarry.platform;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.item.v1.EnchantmentEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.util.TriState;
import pw.rxj.iron_quarry.event.*;

public class FabricEventBridge {
    public static void register() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> ServerLifecycleCallback.STARTING.invoker().onStarting(server));

        ServerLevelEvents.LOAD.register((server, serverWorld) -> ServerLevelCallback.LOAD.invoker().onLoad(serverWorld));
        ServerLevelEvents.UNLOAD.register((server, serverWorld) -> ServerLevelCallback.UNLOAD.invoker().onUnload(serverWorld));
        ServerTickEvents.START_LEVEL_TICK.register(serverWorld -> ServerLevelCallback.START_TICK.invoker().onStartTick(serverWorld));

        ServerEntityEvents.ENTITY_LOAD.register((entity, serverWorld) -> ServerEntityCallback.LOAD.invoker().onLoad(entity, serverWorld));

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> ServerConnectionCallback.JOIN.invoker().onJoin(handler.player));

        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> PlayerInteractionCallback.ATTACK_BLOCK.invoker().onAttackBlock(player, world, hand, pos, direction));
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> PlayerInteractionCallback.USE_BLOCK.invoker().onUseBlock(player, world, hand, hitResult));

        EnchantmentEvents.ALLOW_ENCHANTING.register((enchantment, target, enchantingContext) -> {
            Boolean allowed = EnchantmentCallback.ALLOW_ENCHANTING.invoker().onAllowEnchanting(enchantment, target);

            return allowed == null ? TriState.DEFAULT : TriState.of(allowed);
        });

        LootTableEvents.MODIFY.register((registryKey, builder, lootTableSource, registries) -> LootTableCallback.MODIFY.invoker().onModify(registryKey.identifier(), builder::withPool));
    }

    @Environment(EnvType.CLIENT)
    public static void registerClient() {
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientConnectionCallback.DISCONNECT.invoker().onDisconnect());

        ClientPreAttackCallback.EVENT.register((client, player, clickCount) -> ClientInteractionCallback.PRE_ATTACK.invoker().onPreAttack(player, clickCount));

        LevelRenderEvents.END_MAIN.register(context -> WorldRenderCallback.END_MAIN.invoker().endMain(context.levelState().cameraRenderState, context.poseStack()));
    }
}
