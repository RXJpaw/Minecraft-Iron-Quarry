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
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.block.QuarryBlock;
import pw.rxj.iron_quarry.item.BlueprintItem;
import pw.rxj.iron_quarry.resource.ResourceReloadListener;
import pw.rxj.iron_quarry.util.ZUtil;

import java.util.HashSet;


public class BlueprintPreviewRenderer {
    public static final Identifier BLUEPRINT_PREVIEW_TEXTURE = Identifier.of(Main.MOD_ID, "textures/world/blueprint_preview.png");

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

        int light = 14680272;
        float tickDelta = context.tickDelta();
        double viewDistance = Math.min(minecraftClient.options.getClampedViewDistance() * 16 * 4, 2048.0);

        Camera camera = context.camera();
        MatrixStack matrices = context.matrixStack();
        LightmapTextureManager lightmapTextureManager = context.lightmapTextureManager();

        matrices.push();

        Vec3d lerpedPlayerPos = player.getLerpedPos(tickDelta);

        Vec3d lowest = new Vec3d(
                Math.min(firstPos.getX(), secondPos.getX()),
                Math.min(firstPos.getY(), secondPos.getY()),
                Math.min(firstPos.getZ(), secondPos.getZ())
        ).subtract(lerpedPlayerPos);
        Vec3d highest = new Vec3d(
                Math.max(firstPos.getX(), secondPos.getX()),
                Math.max(firstPos.getY(), secondPos.getY()),
                Math.max(firstPos.getZ(), secondPos.getZ())
        ).subtract(lerpedPlayerPos).add(1, 1, 1);

        Vec3d limitedLowest = RenderUtil.minMaxVec3d(lowest, viewDistance, -viewDistance);
        Vec3d limitedHighest = RenderUtil.minMaxVec3d(highest, viewDistance, -viewDistance);
        HashSet<Direction> limitedDirections = RenderUtil.limitedDirections(lowest, limitedLowest, highest, limitedHighest);

        Vec3d playerToCameraPos = lerpedPlayerPos.subtract(camera.getPos());
        matrices.translate(playerToCameraPos.x, playerToCameraPos.y, playerToCameraPos.z);

        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        lightmapTextureManager.enable();

        RenderSystem.setShader(GameRenderer::getRenderTypeTranslucentShader);
        RenderSystem.setShaderTexture(0, BLUEPRINT_PREVIEW_TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.depthFunc(GL11.GL_ALWAYS);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();

        /*
         * WorldRenderEvents.END doesn't call this when the world border is rendered.
         * Resulting in transparency and brightness being way off.
         */
        RenderSystem.defaultBlendFunc();

        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);

        float minU = 0.0F;
        float maxU = 1.0F;
        float minV = 0.0F;
        float maxV = 1.0F;

        for (Direction direction : Direction.values()) {
            if(limitedDirections.contains(direction)) continue;

            SpriteVec anchors = RenderUtil.anchorsFrom(direction, new Vec3f(limitedLowest), new Vec3f(limitedHighest));

            buffer.vertex(positionMatrix, anchors.tl.getX(), anchors.tl.getY(), anchors.tl.getZ()).color(0.2F, 0.2F, 0.2F, 1.0F).texture(minU, minV).light(light).normal(0, 0, 0).next();
            buffer.vertex(positionMatrix, anchors.bl.getX(), anchors.bl.getY(), anchors.bl.getZ()).color(1.0F, 1.0F, 1.0F, 1.0F).texture(minU, maxV).light(light).normal(0, 0, 0).next();
            buffer.vertex(positionMatrix, anchors.br.getX(), anchors.br.getY(), anchors.br.getZ()).color(0.2F, 0.2F, 0.2F, 1.0F).texture(maxU, maxV).light(light).normal(0, 0, 0).next();
            buffer.vertex(positionMatrix, anchors.tr.getX(), anchors.tr.getY(), anchors.tr.getZ()).color(1.0F, 1.0F, 1.0F, 1.0F).texture(maxU, minV).light(light).normal(0, 0, 0).next();
        }

        tessellator.draw();

        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();

        lightmapTextureManager.disable();

        matrices.pop();
    }

    public static void register() {
        ResourceReloadListener.include(BLUEPRINT_PREVIEW_TEXTURE);
        WorldRenderEvents.END.register(BlueprintPreviewRenderer::render);
    }
}
