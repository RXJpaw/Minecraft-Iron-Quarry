package pw.rxj.iron_quarry.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import pw.rxj.iron_quarry.block.QuarryBlock;
import pw.rxj.iron_quarry.item.BlueprintItem;
import pw.rxj.iron_quarry.util.ZUtil;

import java.util.List;


public class BlueprintPreviewRenderer {
    private static void render(WorldRenderContext context) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
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

        RegistryKey<World> worldRegistryKey = blueprintItem.getWorldRegistryKey(blueprintStack);
        if(!minecraftClient.world.getRegistryKey().equals(worldRegistryKey)) return;
        BlockPos firstPos = blueprintItem.getFirstPos(blueprintStack);
        if(firstPos == null) return;
        BlockPos secondPos = blueprintItem.getSecondPos(blueprintStack);
        if(secondPos == null) return;

        float tickDelta = context.tickDelta();
        double viewDistance = Math.min(minecraftClient.options.getClampedViewDistance() * 16 * 3, 1536.0);
        double squaredViewDistance = Math.pow(viewDistance, 2);
        Camera camera = context.camera();
        MatrixStack matrices = context.matrixStack();

        matrices.push();

        Vec3d lerpedPlayerPos = player.getLerpedPos(tickDelta);

        Cuboid originalCuboid = Cuboid.from(firstPos, secondPos).subtract(lerpedPlayerPos).fullblock();
        Cuboid viewDistanceCuboid = Cuboid.from(viewDistance, -viewDistance);
        Cuboid limitedCuboid = originalCuboid.limitInside(viewDistanceCuboid);
        if(limitedCuboid.isFlat()) return;

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

    public static void register() {
        WorldRenderEvents.END.register(BlueprintPreviewRenderer::render);
    }
}
