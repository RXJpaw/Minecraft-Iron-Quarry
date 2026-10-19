package pw.rxj.iron_quarry.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import oshi.util.tuples.Quartet;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.block.QuarryBlock;
import pw.rxj.iron_quarry.event.InGameHudRenderCallback;
import pw.rxj.iron_quarry.item.BlueprintItem;
import pw.rxj.iron_quarry.item.QuarryMonitorItem;
import pw.rxj.iron_quarry.resource.ResourceReloadListener;
import pw.rxj.iron_quarry.util.ItemStackUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;


public class BlueprintPreviewRenderer {
    public static final Identifier BLUEPRINT_POSITIONS_TEXTURE = Identifier.of(Main.MOD_ID, "textures/gui/blueprint_positions.png");

    private static class ScreenPosData extends Quartet<ScreenPos, Double, Vec2f, Float> {
        public ScreenPosData(ScreenPos screenPos, Double distance, Vec2f texUV, Float alpha) {
            super(screenPos, distance, texUV, alpha);
        }
    }
    private static final List<ScreenPosData> screenPosList = new ArrayList<>();

    private static void renderInWorld(WorldRenderContext context) {
        screenPosList.clear();

        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        if(minecraftClient.options.hudHidden) return;
        if(minecraftClient.world == null) return;
        ClientPlayerEntity player = minecraftClient.player;
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

        RegistryKey<World> worldRegistryKey = blueprintItem.getWorldRegistryKey(blueprintStack).orElse(null);
        if(!minecraftClient.world.getRegistryKey().equals(worldRegistryKey)) return;

        Camera camera = context.camera();
        MatrixStack matrices = context.matrixStack();
        if(matrices == null) return;
        Matrix4f positionMatrix = context.positionMatrix();
        Matrix4f projectionMatrix = context.projectionMatrix();

        BlockPos firstPos = blueprintItem.getFirstPos(blueprintStack).orElse(null);
        BlockPos secondPos = blueprintItem.getSecondPos(blueprintStack).orElse(null);
        prepareRenderOnScreen(firstPos, secondPos, camera, positionMatrix, projectionMatrix);
        if(firstPos == null || secondPos == null) return;

        float tickDelta = context.tickDelta();
        int viewDistance = Math.min(minecraftClient.options.getClampedViewDistance() * 16 * 3, RenderUtil.MAX_VIEW_DISTANCE * 16 * 3);

        Vec3d lerpedPlayerPos = player.getLerpedPos(tickDelta);

        Cuboid originalCuboid = Cuboid.from(firstPos, secondPos).subtract(lerpedPlayerPos).fullblock();
        Cuboid viewDistanceCuboid = Cuboid.from(viewDistance, -viewDistance);
        Cuboid limitedCuboid = originalCuboid.limitInside(viewDistanceCuboid);
        if(limitedCuboid.isFlat()) return;

        matrices.push();

        Vec3d playerToCameraPos = lerpedPlayerPos.subtract(camera.getPos());
        matrices.translate(playerToCameraPos.x, playerToCameraPos.y, playerToCameraPos.z);

        // WorldRenderEvents.END doesn't call this when the world border is rendered.
        // Resulting in color, transparency and brightness being way off.
        RenderSystem.setShaderColor(0.976F, 0.718F, 0.192F, 0.667F);

        RenderUtil.drawZBufferedCuboid(limitedCuboid.inflate(0.02), matrices, viewDistance);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        matrices.pop();
    }

    private static void prepareRenderOnScreen(@Nullable BlockPos firstPos, @Nullable BlockPos secondPos, Camera camera, Matrix4f positionMatrix, Matrix4f projectionMatrix) {
        if(firstPos != null) {
            Vec3d pos1 = RenderUtil.vec3dFrom(firstPos);

            screenPosList.add(new ScreenPosData(
                    RenderUtil.worldToScreen(pos1, positionMatrix, projectionMatrix),
                    pos1.distanceTo(camera.getPos()),
                    new Vec2f(0, 0),
                    1.0F
            ));
        }

        if(secondPos != null) {
            Vec3d pos2 = RenderUtil.vec3dFrom(secondPos);

            screenPosList.add(new ScreenPosData(
                    RenderUtil.worldToScreen(pos2, positionMatrix, projectionMatrix),
                    pos2.distanceTo(camera.getPos()),
                    new Vec2f(13, 0),
                    1.0F
            ));
        }

        if(firstPos != null && secondPos != null) {
            screenPosList.sort((a, b) -> Double.compare(b.getB(), a.getB()));

            ScreenPosData background = screenPosList.get(0);
            ScreenPosData foreground = screenPosList.get(1);

            float distance = (float) background.getA().distanceTo(foreground.getA());

            if(foreground.getA().isAhead() && distance < 20.0F) {
                float alpha = Math.max(Math.min(1.0F, (distance - 4.0F) / 16.0F), 0.2F);

                screenPosList.set(0, new ScreenPosData(
                        background.getA(),
                        background.getB(),
                        background.getC(),
                        alpha
                ));
            }
        }
    }

    private static void renderOnScreen(DrawContext context, double tickDelta) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        TextRenderer textRenderer = minecraftClient.textRenderer;
        MatrixStack matrices = context.getMatrices();

        for (ScreenPosData pair : screenPosList) {
            ScreenPos screenPos = pair.getA();
            if(screenPos.isBehind()) continue;
            double distance = pair.getB();
            Vec2f texUV = pair.getC();
            float alpha = pair.getD();

            float scale = (float) Math.max(1.0, (3.0 / Math.pow(distance, 0.6)));
            //Adjust scale for low resolution displays that default to GUI scale 1.
            if(minecraftClient.getWindow().getScaleFactor() <= 1) scale *= 1.5F;

            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);

            matrices.push();

            matrices.translate(screenPos.x, screenPos.y, 90.0);
            matrices.scale(scale, scale, scale);
            matrices.translate(-6, -6, 0.0);

            context.drawTexture(BLUEPRINT_POSITIONS_TEXTURE, 0, 0, 0, texUV.x, texUV.y, 13, 13, 36, 36);

            matrices.pop();

            //Add 1 to shift the center, because the texture had to be offset.
            if(screenPos.add(1, 1).distanceToCenter() <= 14 * scale) {
                matrices.push();

                matrices.translate(screenPos.x, screenPos.y + 9 * scale, 0.0F);

                MutableText text = Text.literal(String.format("%,.1fm", distance));
                int width = textRenderer.getWidth(text);
                int height = 8;
                scale /= 1.5F;

                matrices.scale(scale, scale, scale);
                matrices.translate(1 + width / -2.0F, 0.0, 0.0);

                context.fill(-2, -2, width + 1, height + 1, 1409286144);
                context.drawText(textRenderer, text, 0, 0, new Color(1.0F, 1.0F, 1.0F, alpha).getRGB(), true);

                matrices.pop();
            }

            RenderSystem.disableBlend();
        }
    }

    public static void register() {
        ResourceReloadListener.include(BLUEPRINT_POSITIONS_TEXTURE);

        WorldRenderEvents.AFTER_TRANSLUCENT.register(BlueprintPreviewRenderer::renderInWorld);
        InGameHudRenderCallback.START.register(BlueprintPreviewRenderer::renderOnScreen);
    }
}
