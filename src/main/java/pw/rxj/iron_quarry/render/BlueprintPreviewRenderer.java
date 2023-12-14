package pw.rxj.iron_quarry.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import oshi.util.tuples.Triplet;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.block.QuarryBlock;
import pw.rxj.iron_quarry.event.InGameHudRenderCallback;
import pw.rxj.iron_quarry.item.BlueprintItem;
import pw.rxj.iron_quarry.resource.ResourceReloadListener;
import pw.rxj.iron_quarry.util.ZUtil;

import java.util.ArrayList;
import java.util.List;


public class BlueprintPreviewRenderer {
    public static final Identifier BLUEPRINT_POSITIONS_TEXTURE = Identifier.of(Main.MOD_ID, "textures/gui/blueprint_positions.png");

    private static final List<Triplet<ScreenPos, Double, Vec2f>> screenPositions = new ArrayList<>();

    private static void renderInWorld(WorldRenderContext context) {
        screenPositions.clear();

        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        if(minecraftClient.options.hudHidden) return;
        if(minecraftClient.world == null) return;
        ClientPlayerEntity player = minecraftClient.player;
        if(player == null) return;

        final ItemStack blueprintStack;

        ItemStack stackInMainHand = player.getStackInHand(Hand.MAIN_HAND);
        ItemStack stackInOffHand = player.getStackInHand(Hand.OFF_HAND);

        if(BlueprintItem.isOf(stackInMainHand)) {
            blueprintStack = stackInMainHand;
        } else if(BlueprintItem.isOf(stackInOffHand)) {
            blueprintStack = stackInOffHand;
        } else if(ZUtil.getBlockOrItem(stackInMainHand) instanceof QuarryBlock quarryBlock) {
            ItemStack stack = quarryBlock.getBlueprintStack(stackInMainHand);
            if(stack.isEmpty()) return;
            blueprintStack = stack;
        } else if(ZUtil.getBlockOrItem(stackInOffHand) instanceof QuarryBlock quarryBlock) {
            ItemStack stack = quarryBlock.getBlueprintStack(stackInOffHand);
            if(stack.isEmpty()) return;
            blueprintStack = stack;
        } else return;

        final BlueprintItem blueprintItem = (BlueprintItem) blueprintStack.getItem();

        RegistryKey<World> worldRegistryKey = blueprintItem.getWorldRegistryKey(blueprintStack).orElse(null);
        if(!minecraftClient.world.getRegistryKey().equals(worldRegistryKey)) return;

        Camera camera = context.camera();
        MatrixStack matrices = context.matrixStack();
        Matrix4f projectionMatrix = context.projectionMatrix();

        BlockPos firstPos = blueprintItem.getFirstPos(blueprintStack).orElse(null);
        BlockPos secondPos = blueprintItem.getSecondPos(blueprintStack).orElse(null);
        prepareRenderOnScreen(matrices, firstPos, secondPos, camera, projectionMatrix);
        if(firstPos == null || secondPos == null) return;

        float tickDelta = context.tickDelta();
        double viewDistance = Math.min(minecraftClient.options.getClampedViewDistance() * 16 * 3, 1536.0);
        double squaredViewDistance = Math.pow(viewDistance, 2);

        Vec3d lerpedPlayerPos = player.getLerpedPos(tickDelta);

        Cuboid originalCuboid = Cuboid.from(firstPos, secondPos).subtract(lerpedPlayerPos).fullblock();
        Cuboid viewDistanceCuboid = Cuboid.from(viewDistance, -viewDistance);
        Cuboid limitedCuboid = originalCuboid.limitInside(viewDistanceCuboid);
        if(limitedCuboid.isFlat()) return;

        matrices.push();

        Vec3d playerToCameraPos = lerpedPlayerPos.subtract(camera.getPos());
        matrices.translate(playerToCameraPos.x, playerToCameraPos.y, playerToCameraPos.z);

        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        // WorldRenderEvents.END doesn't call this when the world border is rendered.
        // Resulting in color, transparency and brightness being way off.
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.defaultBlendFunc();

        //Outlines
        RenderSystem.setShader(GameRenderer::getRenderTypeLinesShader);
        RenderSystem.lineWidth(3.0F);
        RenderSystem.enableBlend();
        RenderSystem.disableCull();

        double outlineOffset = 0.02;

        List<SpriteVec2f> boxLines = limitedCuboid.inflate(outlineOffset).getLines();
        List<SpriteVec2f> splitBoxLines = boxLines.stream().map(vec -> vec.autoSplit(8.0F)).flatMap(List::stream).toList();
        List<SpriteVec2f> filteredSplitBoxLines = splitBoxLines.stream().filter(vec -> vec.squaredDistanceTo(Vec3f.ZERO) <= squaredViewDistance).toList();

        //Visible Outlines
        RenderSystem.depthFunc(GL11.GL_LESS);
        buffer.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);

        for (SpriteVec2f line : filteredSplitBoxLines) {
            Vec3f n = line.normalize();

            buffer.vertex(positionMatrix, line.from.getX(), line.from.getY(), line.from.getZ()).color(1.0F, 1.0F, 1.0F, 1.0F).normal(matrices.peek().getNormalMatrix(), n.getX(), n.getY(), n.getZ()).next();
            buffer.vertex(positionMatrix, line.to.getX(), line.to.getY(), line.to.getZ()).color(1.0F, 1.0F, 1.0F, 1.0F).normal(matrices.peek().getNormalMatrix(), n.getX(), n.getY(), n.getZ()).next();
        }

        tessellator.draw();

        //Hidden Outlines
        RenderSystem.depthFunc(GL11.GL_GREATER);
        buffer.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);

        for (SpriteVec2f line : filteredSplitBoxLines) {
            Vec3f n = line.normalize();

            buffer.vertex(positionMatrix, line.from.getX(), line.from.getY(), line.from.getZ()).color(1.0F, 1.0F, 1.0F, 0.2F).normal(matrices.peek().getNormalMatrix(), n.getX(), n.getY(), n.getZ()).next();
            buffer.vertex(positionMatrix, line.to.getX(), line.to.getY(), line.to.getZ()).color(1.0F, 1.0F, 1.0F, 0.2F).normal(matrices.peek().getNormalMatrix(), n.getX(), n.getY(), n.getZ()).next();
        }

        tessellator.draw();

        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.lineWidth(1.0F);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();

        matrices.pop();
    }

    private static void prepareRenderOnScreen(MatrixStack matrices, @Nullable BlockPos firstPos, @Nullable BlockPos secondPos, Camera camera, Matrix4f projectionMatrix) {
        if(firstPos != null) {
            Vec3d pos1 = RenderUtil.vec3dFrom(firstPos);

            screenPositions.add(new Triplet<>(
                    RenderUtil.worldToScreen(pos1, matrices.peek().getPositionMatrix(), projectionMatrix),
                    pos1.distanceTo(camera.getPos()),
                    new Vec2f(0, 0)
            ));
        }

        if(secondPos != null) {
            Vec3d pos2 = RenderUtil.vec3dFrom(secondPos);

            screenPositions.add(new Triplet<>(
                    RenderUtil.worldToScreen(pos2, matrices.peek().getPositionMatrix(), projectionMatrix),
                    pos2.distanceTo(camera.getPos()),
                    new Vec2f(13, 0)
            ));
        }

        if(firstPos != null && secondPos != null) {
            screenPositions.sort((a, b) -> Double.compare(b.getB(), a.getB()));
        }
    }

    private static void renderOnScreen(MatrixStack matrices, double tickDelta) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        TextRenderer textRenderer = minecraftClient.textRenderer;

        for (Triplet<ScreenPos, Double, Vec2f> pair : screenPositions) {
            ScreenPos screenPos = pair.getA();
            if(screenPos.isBehind()) continue;
            double distance = pair.getB();
            Vec2f uv = pair.getC();

            float scale = (float) Math.max(1.0, (3.0 / Math.pow(distance, 0.6)));
            //Adjust scale for low resolution displays that default to GUI scale 1.
            if(minecraftClient.getWindow().getScaleFactor() <= 1) scale *= 1.5F;

            RenderSystem.enableBlend();
            RenderSystem.setShaderTexture(0, BLUEPRINT_POSITIONS_TEXTURE);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            matrices.push();
            matrices.translate(screenPos.x, screenPos.y, 90.0);
            matrices.scale(scale, scale, scale);
            //Align on full pixel to prevent AA artifacts. ^1
            matrices.translate(-6, -6, 0.0);

            DrawableHelper.drawTexture(matrices, 0, 0, 0, uv.x, uv.y, 13, 13, 36, 36);

            matrices.pop();

            //Add 1 to shift the center, because the texture had to be offset.
            if(screenPos.add(1, 1).distanceToCenter() <= 14 * scale) {
                matrices.push();
                matrices.translate(screenPos.x, screenPos.y + 9 * scale, 90.0);

                MutableText text = Text.literal(String.format("%,.1fm", distance));
                int width = textRenderer.getWidth(text);
                int height = 8;
                scale /= 1.5F;

                matrices.scale(scale, scale, scale);
                matrices.translate(1 + width / -2.0F, 0.0, 0.0);

                DrawableHelper.fill(matrices, -2, -2, width + 1, height + 1, 1409286144);
                textRenderer.draw(matrices, text, 0, 0, -1);

                matrices.pop();
            }

            RenderSystem.disableBlend();
        }
    }

    public static void register() {
        ResourceReloadListener.include(BLUEPRINT_POSITIONS_TEXTURE);

        WorldRenderEvents.END.register(BlueprintPreviewRenderer::renderInWorld);
        InGameHudRenderCallback.START.register(BlueprintPreviewRenderer::renderOnScreen);
    }
}
