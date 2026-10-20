package pw.rxj.iron_quarry.render;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;
import org.joml.Matrix4f;
import oshi.util.tuples.Quartet;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.block.QuarryBlock;
import pw.rxj.iron_quarry.event.InGameHudRenderCallback;
import pw.rxj.iron_quarry.event.WorldRenderCallback;
import pw.rxj.iron_quarry.item.BlueprintItem;
import pw.rxj.iron_quarry.item.QuarryMonitorItem;
import pw.rxj.iron_quarry.resource.ResourceReloadListener;
import pw.rxj.iron_quarry.util.ItemStackUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;


public class BlueprintPreviewRenderer {
    public static final Identifier BLUEPRINT_POSITIONS_TEXTURE = Identifier.fromNamespaceAndPath(Main.MOD_ID, "textures/gui/blueprint_positions.png");

    private static class ScreenPosData extends Quartet<ScreenPos, Double, Vec2, Float> {
        public ScreenPosData(ScreenPos screenPos, Double distance, Vec2 texUV, Float alpha) {
            super(screenPos, distance, texUV, alpha);
        }
    }
    private static final List<ScreenPosData> screenPosList = new ArrayList<>();

    private static void renderInWorld(WorldRenderCallback.Context context) {
        screenPosList.clear();

        Minecraft minecraftClient = Minecraft.getInstance();
        if(minecraftClient.options.hideGui) return;
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

        Camera camera = context.camera();
        PoseStack matrices = context.matrixStack();
        if(matrices == null) return;
        Matrix4f positionMatrix = context.positionMatrix();
        Matrix4f projectionMatrix = context.projectionMatrix();

        BlockPos firstPos = blueprintItem.getFirstPos(blueprintStack).orElse(null);
        BlockPos secondPos = blueprintItem.getSecondPos(blueprintStack).orElse(null);
        prepareRenderOnScreen(firstPos, secondPos, camera, positionMatrix, projectionMatrix);
        if(firstPos == null || secondPos == null) return;

        float tickDelta = context.tickCounter().getGameTimeDeltaPartialTick(false);
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
        RenderUtil.drawZBufferedCuboid(limitedCuboid.inflate(0.02), matrices, viewDistance, 0.976F, 0.718F, 0.192F, 0.667F);

        matrices.popPose();
    }

    private static void prepareRenderOnScreen(@Nullable BlockPos firstPos, @Nullable BlockPos secondPos, Camera camera, Matrix4f positionMatrix, Matrix4f projectionMatrix) {
        if(firstPos != null) {
            Vec3 pos1 = RenderUtil.vec3dFrom(firstPos);

            screenPosList.add(new ScreenPosData(
                    RenderUtil.worldToScreen(pos1, positionMatrix, projectionMatrix),
                    pos1.distanceTo(camera.position()),
                    new Vec2(0, 0),
                    1.0F
            ));
        }

        if(secondPos != null) {
            Vec3 pos2 = RenderUtil.vec3dFrom(secondPos);

            screenPosList.add(new ScreenPosData(
                    RenderUtil.worldToScreen(pos2, positionMatrix, projectionMatrix),
                    pos2.distanceTo(camera.position()),
                    new Vec2(13, 0),
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

    private static void renderOnScreen(GuiGraphics context, double tickDelta) {
        Minecraft minecraftClient = Minecraft.getInstance();
        Font textRenderer = minecraftClient.font;
        Matrix3x2fStack matrices = context.pose();

        for (ScreenPosData pair : screenPosList) {
            ScreenPos screenPos = pair.getA();
            if(screenPos.isBehind()) continue;
            double distance = pair.getB();
            Vec2 texUV = pair.getC();
            float alpha = pair.getD();

            float scale = (float) Math.max(1.0, (3.0 / Math.pow(distance, 0.6)));
            //Adjust scale for low resolution displays that default to GUI scale 1.
            if(minecraftClient.getWindow().getGuiScale() <= 1) scale *= 1.5F;


            matrices.pushMatrix();

            matrices.translate(screenPos.x, screenPos.y);
            matrices.scale(scale, scale);
            matrices.translate(-6, -6);

            RenderUtil.drawTexture(context, BLUEPRINT_POSITIONS_TEXTURE, 0, 0, texUV.x, texUV.y, 13, 13, 36, 36, RenderUtil.whiteWithAlpha(alpha));

            matrices.popMatrix();

            //Add 1 to shift the center, because the texture had to be offset.
            if(screenPos.add(1, 1).distanceToCenter() <= 14 * scale) {
                matrices.pushMatrix();

                matrices.translate(screenPos.x, screenPos.y + 9 * scale);

                MutableComponent text = Component.literal(String.format("%,.1fm", distance));
                int width = textRenderer.width(text);
                int height = 8;
                scale /= 1.5F;

                matrices.scale(scale, scale);
                matrices.translate(1 + width / -2.0F, 0.0F);

                context.fill(-2, -2, width + 1, height + 1, 1409286144);
                context.drawString(textRenderer, text, 0, 0, new Color(1.0F, 1.0F, 1.0F, alpha).getRGB(), true);

                matrices.popMatrix();
            }

        }
    }

    public static void register() {
        ResourceReloadListener.include(BLUEPRINT_POSITIONS_TEXTURE);

        WorldRenderCallback.AFTER_TRANSLUCENT.register(BlueprintPreviewRenderer::renderInWorld);
        InGameHudRenderCallback.START.register(BlueprintPreviewRenderer::renderOnScreen);
    }
}
