package pw.rxj.iron_quarry.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.block.QuarryBlock;
import pw.rxj.iron_quarry.event.InGameHudRenderCallback;
import pw.rxj.iron_quarry.event.WorldRenderCallback;
import pw.rxj.iron_quarry.item.BlueprintItem;
import pw.rxj.iron_quarry.item.QuarryMonitorItem;
import pw.rxj.iron_quarry.resource.ResourceReloadListener;
import pw.rxj.iron_quarry.resource.config.client.BlueprintPreviewConfig;
import pw.rxj.iron_quarry.util.ItemStackUtil;

import java.util.concurrent.atomic.AtomicReference;


public class BlueprintPreviewRenderer {
    private static final BlueprintPreviewConfig.Handler BLUEPRINT_PREVIEW_CONFIG = Main.CONFIG.getBlueprintPreviewConfig();
    private static final HudBlueprintMarkerRenderer HUD_MARKER_RENDERER = new HudBlueprintMarkerRenderer();
    private static final WorldBlueprintMarkerRenderer WORLD_MARKER_RENDERER = new WorldBlueprintMarkerRenderer();

    private static void renderInWorld(LevelRenderContext context) {
        HUD_MARKER_RENDERER.clear();
        WORLD_MARKER_RENDERER.clear();

        Minecraft minecraftClient = Minecraft.getInstance();
        if(minecraftClient.gui.hud.isHidden()) return;
        if(minecraftClient.level == null) return;
        LocalPlayer player = minecraftClient.player;
        if(player == null) return;

        AtomicReference<ItemStack> atomicBlueprintStack = new AtomicReference<>();

        ItemStackUtil.ofAnyHand(player, BlueprintItem.class).ifPresent(stackUtil -> {
            atomicBlueprintStack.set(stackUtil.stack);
        });
        if(atomicBlueprintStack.get() == null) {
            ItemStackUtil.ofAnyHand(player, QuarryBlock.class).ifPresent(stackUtil -> {
                ItemStack stack = stackUtil.item.getBlueprintStack(stackUtil.stack);
                if(!stack.isEmpty()) atomicBlueprintStack.set(stack);
            });
        }
        if(atomicBlueprintStack.get() == null) {
            ItemStackUtil.ofAnyHand(player, QuarryMonitorItem.class).ifPresent(stackUtil -> {
                ItemStack stack = stackUtil.item.getCachedBlueprintStack(stackUtil.stack);
                if(!stack.isEmpty()) atomicBlueprintStack.set(stack);
            });
        }

        final ItemStack blueprintStack = atomicBlueprintStack.get();
        if(blueprintStack == null) return;
        final BlueprintItem blueprintItem = (BlueprintItem) blueprintStack.getItem();

        ResourceKey<Level> worldRegistryKey = blueprintItem.getWorldRegistryKey(blueprintStack).orElse(null);
        if(!minecraftClient.level.dimension().equals(worldRegistryKey)) return;

        CameraRenderState cameraRenderState = context.levelState().cameraRenderState;
        Camera camera = minecraftClient.gameRenderer.mainCamera();
        PoseStack matrices = context.poseStack();
        if(matrices == null) return;
        Matrix4f positionMatrix = cameraRenderState.viewRotationMatrix;
        Matrix4f projectionMatrix = cameraRenderState.getDataOrDefault(RenderUtil.RenderStates.PROJECTION_MATRIX, cameraRenderState.projectionMatrix);

        BlockPos firstPos = blueprintItem.getFirstPos(blueprintStack).orElse(null);
        BlockPos secondPos = blueprintItem.getSecondPos(blueprintStack).orElse(null);
        if(firstPos != null && secondPos != null) renderCuboid(minecraftClient, player, camera, context.submitNodeCollector(), matrices, firstPos, secondPos);

        BlueprintMarkerRenderer markerRenderer = switch (BLUEPRINT_PREVIEW_CONFIG.getMarkerRenderMode()) {
            case HUD -> HUD_MARKER_RENDERER;
            case WORLD -> WORLD_MARKER_RENDERER;
        };

        markerRenderer.update(firstPos, secondPos, camera, positionMatrix, projectionMatrix);
    }

    private static void renderCuboid(Minecraft minecraftClient, LocalPlayer player, Camera camera, SubmitNodeCollector submitNodeCollector, PoseStack matrices, BlockPos firstPos, BlockPos secondPos) {
        float tickDelta = minecraftClient.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        int viewDistance = Math.min(minecraftClient.options.getEffectiveRenderDistance() * 16 * 3, RenderUtil.MAX_VIEW_DISTANCE * 16 * 3);

        Vec3 lerpedPlayerPos = player.getPosition(tickDelta);

        Cuboid originalCuboid = Cuboid.from(firstPos, secondPos).subtract(lerpedPlayerPos).fullblock();
        Cuboid viewDistanceCuboid = Cuboid.from(viewDistance, -viewDistance);
        Cuboid limitedCuboid = originalCuboid.limitInside(viewDistanceCuboid);
        if(limitedCuboid.isFlat()) return;

        matrices.pushPose();

        Vec3 playerToCameraPos = lerpedPlayerPos.subtract(camera.position());
        matrices.translate(playerToCameraPos.x, playerToCameraPos.y, playerToCameraPos.z);

        // WorldRenderEvents.END doesn't call this when the world border is rendered.
        // Resulting in color, transparency and brightness being way off.
        RenderUtil.drawZBufferedCuboid(submitNodeCollector, limitedCuboid.inflate(0.02), matrices, camera.forwardVector(), viewDistance, 0.976F, 0.718F, 0.192F, 0.667F);

        matrices.popPose();
    }

    public static void register() {
        ResourceReloadListener.include(BlueprintMarkerRenderer.BLUEPRINT_POSITIONS_TEXTURE);

        LevelRenderEvents.COLLECT_SUBMITS.register(BlueprintPreviewRenderer::renderInWorld);

        WorldRenderCallback.AFTER_LEVEL.register(WORLD_MARKER_RENDERER::render);
        InGameHudRenderCallback.START.register(HUD_MARKER_RENDERER::render);
    }
}
