package pw.rxj.iron_quarry.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.*;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import pw.rxj.iron_quarry.blockentities.QuarryBlockEntity;
import pw.rxj.iron_quarry.blocks.QuarryBlock;
import pw.rxj.iron_quarry.util.Color;
import pw.rxj.iron_quarry.records.TexturePosition;
import pw.rxj.iron_quarry.types.Face;

public class QuarryBlockEntityRenderer implements BlockEntityRenderer<QuarryBlockEntity> {
    private final TextRenderer textRenderer;

    public QuarryBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this.textRenderer = ctx.getTextRenderer();
    }

    @Override
    public void render(QuarryBlockEntity blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumerProvider, int light, int overlay) {
        World world = blockEntity.getWorld();
        if (world == null) return;

        BlockPos blockPos = blockEntity.getPos();
        BlockState blockState = world.getBlockState(blockPos);

        if (!(blockState.getBlock() instanceof QuarryBlock quarryBlock)) return;

        Direction facing = blockState.get(QuarryBlock.FACING);

        //Rendering
        MinecraftClient.getInstance().gameRenderer.getLightmapTextureManager().enable();

        RenderSystem.setShader(GameRenderer::getRenderTypeTranslucentShader);
        RenderSystem.setShaderTexture(0, blockEntity.getIoTextureId());
        RenderSystem.enableDepthTest();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);

        for (Face face : Face.values()) {
            BooleanProperty facingProperty = QuarryBlock.getFacingProperty(face);
            if (blockState.get(facingProperty).equals(false)) continue;

            TexturePosition texture = blockEntity.getIoTexturePosition(face);
            if (texture.u() == 0 && texture.v() == 0) continue;

            Direction direction = face.toDirection(facing);
            if (direction == null) continue;

            Color color_tl = new Color(1.0f, 1.0f, 1.0f);
            Color color_bl = new Color(1.0f, 1.0f, 1.0f);
            Color color_br = new Color(1.0f, 1.0f, 1.0f);
            Color color_tr = new Color(1.0f, 1.0f, 1.0f);

            float minU = texture.minU(256);
            float maxU = texture.maxU(256);
            float minV = texture.minV(256);
            float maxV = texture.maxV(256);

            Vec3f anchor_tl = null;
            Vec3f anchor_bl = null;
            Vec3f anchor_br = null;
            Vec3f anchor_tr = null;

            int block_light = world.getLightLevel(LightType.BLOCK, blockPos.add(direction.getVector()));
            int sky_light = world.getLightLevel(LightType.SKY, blockPos.add(direction.getVector()));
            light = sky_light * 1_048_576 + block_light * 16;

            float min = 0.25F;
            float max = 0.75F;
            float depthMax = 1.0F;
            float depthMin = 0.0F;

            switch (direction) {
                case UP -> {
                    anchor_tl = new Vec3f(max, depthMax, max); //oben links
                    anchor_bl = new Vec3f(max, depthMax, min); //unten links
                    anchor_br = new Vec3f(min, depthMax, min); //unten rechts
                    anchor_tr = new Vec3f(min, depthMax, max); //oben rechts
                }
                case NORTH -> {
                    anchor_tl = new Vec3f(max, max, depthMin);
                    anchor_bl = new Vec3f(max, min, depthMin);
                    anchor_br = new Vec3f(min, min, depthMin);
                    anchor_tr = new Vec3f(min, max, depthMin);
                }
                case WEST -> {
                    anchor_tl = new Vec3f(depthMin, max, min);
                    anchor_bl = new Vec3f(depthMin, min, min);
                    anchor_br = new Vec3f(depthMin, min, max);
                    anchor_tr = new Vec3f(depthMin, max, max);
                }
                case SOUTH -> {
                    anchor_tl = new Vec3f(min, max, depthMax);
                    anchor_bl = new Vec3f(min, min, depthMax);
                    anchor_br = new Vec3f(max, min, depthMax);
                    anchor_tr = new Vec3f(max, max, depthMax);
                }
                case EAST -> {
                    anchor_tl = new Vec3f(depthMax, max, max);
                    anchor_bl = new Vec3f(depthMax, min, max);
                    anchor_br = new Vec3f(depthMax, min, min);
                    anchor_tr = new Vec3f(depthMax, max, min);
                }
                case DOWN -> {
                    anchor_tl = new Vec3f(min, depthMin, max);
                    anchor_bl = new Vec3f(min, depthMin, min);
                    anchor_br = new Vec3f(max, depthMin, min);
                    anchor_tr = new Vec3f(max, depthMin, max);
                }
            }

            bufferBuilder.vertex(matrices.peek().getPositionMatrix(), anchor_tl.getX(), anchor_tl.getY(), anchor_tl.getZ()).color(color_tl.r, color_tl.g, color_tl.b, 1).texture(minU, minV).light(light).normal(0, 0, 0).next();
            bufferBuilder.vertex(matrices.peek().getPositionMatrix(), anchor_bl.getX(), anchor_bl.getY(), anchor_bl.getZ()).color(color_bl.r, color_bl.g, color_bl.b, 1).texture(minU, maxV).light(light).normal(0, 0, 0).next();
            bufferBuilder.vertex(matrices.peek().getPositionMatrix(), anchor_br.getX(), anchor_br.getY(), anchor_br.getZ()).color(color_br.r, color_br.g, color_br.b, 1).texture(maxU, maxV).light(light).normal(0, 0, 0).next();
            bufferBuilder.vertex(matrices.peek().getPositionMatrix(), anchor_tr.getX(), anchor_tr.getY(), anchor_tr.getZ()).color(color_tr.r, color_tr.g, color_tr.b, 1).texture(maxU, minV).light(light).normal(0, 0, 0).next();
        }

        BufferRenderer.drawWithShader(bufferBuilder.end());
        RenderSystem.disableDepthTest();

        MinecraftClient.getInstance().gameRenderer.getLightmapTextureManager().disable();
    }
}
