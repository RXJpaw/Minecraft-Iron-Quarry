package pw.rxj.iron_quarry.renderer;

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
import org.lwjgl.opengl.GL11;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.blocks.QuarryBlock;
import pw.rxj.iron_quarry.items.BlueprintItem;
import pw.rxj.iron_quarry.resource.ResourceReloadListener;
import pw.rxj.iron_quarry.util.ZUtil;


public class BlueprintPreviewRenderer {
    public static final Identifier BLUEPRINT_PREVIEW_TEXTURE = Identifier.of(Main.MOD_ID, "textures/world/blueprint_preview.png");

    private static void render(WorldRenderContext context) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
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

        BlockPos firstPos = blueprintItem.getFirstPos(blueprintStack);
        BlockPos secondPos = blueprintItem.getSecondPos(blueprintStack);

        double midX = firstPos.getX() - ((double) (firstPos.getX() - secondPos.getX()) / 2);
        double midY = firstPos.getY() - ((double) (firstPos.getY() - secondPos.getY()) / 2);
        double midZ = firstPos.getZ() - ((double) (firstPos.getZ() - secondPos.getZ()) / 2);

        int absX = Math.abs(firstPos.getX() - secondPos.getX());
        int absY = Math.abs(firstPos.getY() - secondPos.getY());
        int absZ = Math.abs(firstPos.getZ() - secondPos.getZ());

        Camera camera = context.camera();

        Vec3d targetPosition = new Vec3d(midX, midY, midZ);
        Vec3d transformedPosition = targetPosition.subtract(camera.getPos());

        MatrixStack matrixStack = new MatrixStack();
        matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(camera.getPitch()));
        matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(camera.getYaw() + 180.0F));
        matrixStack.translate(transformedPosition.x, transformedPosition.y, transformedPosition.z);

        context.lightmapTextureManager().enable();

        RenderSystem.setShader(GameRenderer::getRenderTypeTranslucentShader);
        RenderSystem.setShaderTexture(0, BLUEPRINT_PREVIEW_TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.depthFunc(GL11.GL_ALWAYS);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();

        Matrix4f positionMatrix = matrixStack.peek().getPositionMatrix();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);

        for (Direction direction : Direction.values()) {
            int light = 14680272;

            float minU = 0.0F;
            float maxU = 1.0F;
            float minV = 0.0F;
            float maxV = 1.0F;

            Vec3f anchor_tl = null;
            Vec3f anchor_bl = null;
            Vec3f anchor_br = null;
            Vec3f anchor_tr = null;

            float minX = -((float) absX / 2);
            float minY = -((float) absY / 2);
            float minZ = -((float) absZ / 2);

            float maxX = 1 + (float) absX / 2;
            float maxY = 1 + (float) absY / 2;
            float maxZ = 1 + (float) absZ / 2;

            switch (direction) {
                case UP -> {
                    anchor_tl = new Vec3f(maxX, maxY, maxZ); //top left
                    anchor_bl = new Vec3f(maxX, maxY, minZ); //bottom left
                    anchor_br = new Vec3f(minX, maxY, minZ); //bottom right
                    anchor_tr = new Vec3f(minX, maxY, maxZ); //top right
                }
                case NORTH -> {
                    anchor_tl = new Vec3f(maxX, maxY, minZ);
                    anchor_bl = new Vec3f(maxX, minY, minZ);
                    anchor_br = new Vec3f(minX, minY, minZ);
                    anchor_tr = new Vec3f(minX, maxY, minZ);
                }
                case WEST -> {
                    anchor_tl = new Vec3f(minX, maxY, minZ);
                    anchor_bl = new Vec3f(minX, minY, minZ);
                    anchor_br = new Vec3f(minX, minY, maxZ);
                    anchor_tr = new Vec3f(minX, maxY, maxZ);
                }
                case SOUTH -> {
                    anchor_tl = new Vec3f(minX, maxY, maxZ);
                    anchor_bl = new Vec3f(minX, minY, maxZ);
                    anchor_br = new Vec3f(maxX, minY, maxZ);
                    anchor_tr = new Vec3f(maxX, maxY, maxZ);
                }
                case EAST -> {
                    anchor_tl = new Vec3f(maxX, maxY, maxZ);
                    anchor_bl = new Vec3f(maxX, minY, maxZ);
                    anchor_br = new Vec3f(maxX, minY, minZ);
                    anchor_tr = new Vec3f(maxX, maxY, minZ);
                }
                case DOWN -> {
                    anchor_tl = new Vec3f(minX, minY, maxZ);
                    anchor_bl = new Vec3f(minX, minY, minZ);
                    anchor_br = new Vec3f(maxX, minY, minZ);
                    anchor_tr = new Vec3f(maxX, minY, maxZ);
                }
            }

            buffer.vertex(positionMatrix, anchor_tl.getX(), anchor_tl.getY(), anchor_tl.getZ()).color(0.2F, 0.2F, 0.2F, 1.0F).texture(minU, minV).light(light).normal(0, 0, 0).next();
            buffer.vertex(positionMatrix, anchor_bl.getX(), anchor_bl.getY(), anchor_bl.getZ()).color(1.0F, 1.0F, 1.0F, 1.0F).texture(minU, maxV).light(light).normal(0, 0, 0).next();
            buffer.vertex(positionMatrix, anchor_br.getX(), anchor_br.getY(), anchor_br.getZ()).color(0.2F, 0.2F, 0.2F, 1.0F).texture(maxU, maxV).light(light).normal(0, 0, 0).next();
            buffer.vertex(positionMatrix, anchor_tr.getX(), anchor_tr.getY(), anchor_tr.getZ()).color(1.0F, 1.0F, 1.0F, 1.0F).texture(maxU, minV).light(light).normal(0, 0, 0).next();
        }

        tessellator.draw();

        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();

        context.lightmapTextureManager().disable();
    }

    public static void register() {
        ResourceReloadListener.include(BLUEPRINT_PREVIEW_TEXTURE);
        WorldRenderEvents.END.register(BlueprintPreviewRenderer::render);
    }
}
