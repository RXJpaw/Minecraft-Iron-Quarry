package pw.rxj.iron_quarry.render;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.renderer.v1.Renderer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableMesh;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3fc;
import pw.rxj.iron_quarry.block.QuarryBlock;
import pw.rxj.iron_quarry.event.WorldRenderCallback;
import pw.rxj.iron_quarry.item.QuarryMonitorItem;
import pw.rxj.iron_quarry.model.QuarryModel;
import pw.rxj.iron_quarry.util.ItemStackUtil;
import pw.rxj.iron_quarry.util.ZUtil;

public class QuarryMonitorPreviewRenderer {
    private static final double MIN_PREVIEW_DEPTH = 1.0;
    private static final float MIN_PREVIEW_SIZE = 16.0F / 3.0F;
    private static final float PREVIEW_ALPHA = 0.66F;
    private static final Block MISSING_QUARRY_BLOCK = Blocks.BEDROCK; //TODO: custom texture

    private static void renderInWorld(CameraRenderState cameraRenderState) {
        Minecraft minecraftClient = Minecraft.getInstance();
        if(minecraftClient.options.hideGui) return;
        ClientLevel world = minecraftClient.level;
        if(world == null) return;
        LocalPlayer player = minecraftClient.player;
        if(player == null) return;

        ItemStackUtil<QuarryMonitorItem> stackUtil = ItemStackUtil.ofAnyHand(player, QuarryMonitorItem.class).orElse(null);
        if(stackUtil == null) return;

        QuarryMonitorItem quarryMonitorItem = stackUtil.item;
        ItemStack quarryMonitorStack = stackUtil.stack;

        ResourceKey<Level> worldRegistryKey = quarryMonitorItem.getWorldRegistryKey(quarryMonitorStack).orElse(null);
        if(!world.dimension().equals(worldRegistryKey)) return;
        BlockPos quarryPos = quarryMonitorItem.getPosition(quarryMonitorStack);
        if(quarryPos == null) return;
        if(minecraftClient.hitResult instanceof BlockHitResult blockHitResult && blockHitResult.getType().equals(HitResult.Type.BLOCK) && blockHitResult.getBlockPos().equals(quarryPos)) return;

        Camera camera = minecraftClient.gameRenderer.getMainCamera();
        Window window = minecraftClient.getWindow();
        Matrix4f projectionMatrix = cameraRenderState.projectionMatrix;

        Vec3 relativePos = RenderUtil.vec3dFrom(quarryPos).subtract(camera.position());
        Vector3fc forward = camera.forwardVector();
        double depth = relativePos.x * forward.x() + relativePos.y * forward.y() + relativePos.z * forward.z();

        double maxDepth = cameraRenderState.depthFar * 0.5;
        float depthScale = depth > maxDepth ? (float) (maxDepth / depth) : 1.0F;
        Vec3 previewPos = relativePos.scale(depthScale);

        float blockSize = (float) (window.getHeight() * projectionMatrix.m11() / (window.getGuiScale() * 2.0 * Math.max(depth, MIN_PREVIEW_DEPTH)));
        float enlargement = Math.max(1.0F, MIN_PREVIEW_SIZE / blockSize);

        ItemStack quarryStack = quarryMonitorItem.getCachedQuarryStack(quarryMonitorStack);
        BlockStateModelSet blockStateModelSet = minecraftClient.getModelManager().getBlockStateModelSet();

        final Mesh mesh;
        final boolean coversRealBlock;

        if(ZUtil.getBlockOrItem(quarryStack) instanceof QuarryBlock quarryBlock) {
            if(!(blockStateModelSet.get(quarryBlock.defaultBlockState()) instanceof QuarryModel quarryModel)) return;

            BlockState blockState = world.getBlockState(quarryPos);
            coversRealBlock = blockState.getBlock() instanceof QuarryBlock && depth <= maxDepth && enlargement <= 1.0F;
            Direction frontDirection = coversRealBlock ? blockState.getValue(QuarryBlock.FACING) : quarryMonitorItem.getCachedFacing(quarryMonitorStack);

            mesh = quarryModel.getPreviewMesh(quarryStack, frontDirection);
        } else {
            coversRealBlock = false;
            mesh = getMissingQuarryMesh(blockStateModelSet);
        }

        if(mesh == null) return;

        float approach = coversRealBlock ? (float) (1.0 - 0.001 - depth * 5.0E-6) : 1.0F;
        float scale = depthScale * enlargement * approach;
        Vec3 modelPos = previewPos.scale(approach);

        PoseStack matrices = new PoseStack();
        matrices.mulPose(cameraRenderState.viewRotationMatrix);
        matrices.translate(modelPos.x, modelPos.y, modelPos.z);
        matrices.scale(scale, scale, scale);
        matrices.translate(-0.5F, -0.5F, -0.5F);

        RenderUtil.drawPreviewMesh(matrices, consumer -> mesh.forEach(quad -> consumer.accept(new RenderUtil.PreviewQuad() {
            @Override
            public float x(int vertex) {
                return quad.x(vertex);
            }
            @Override
            public float y(int vertex) {
                return quad.y(vertex);
            }
            @Override
            public float z(int vertex) {
                return quad.z(vertex);
            }
            @Override
            public float u(int vertex) {
                return quad.u(vertex);
            }
            @Override
            public float v(int vertex) {
                return quad.v(vertex);
            }
            @Override
            public int color(int vertex) {
                return quad.color(vertex);
            }
            @Override
            public boolean diffuseShade() {
                return quad.diffuseShade();
            }
            @Override
            public Direction lightFace() {
                return quad.lightFace();
            }
        })), world.cardinalLighting(), PREVIEW_ALPHA, coversRealBlock);
    }

    private static Mesh getMissingQuarryMesh(BlockStateModelSet blockStateModelSet) {
        BlockState blockState = MISSING_QUARRY_BLOCK.defaultBlockState();
        MutableMesh mesh = Renderer.get().mutableMesh();

        blockStateModelSet.get(blockState).emitQuads(mesh.emitter(), BlockAndTintGetter.EMPTY, BlockPos.ZERO, blockState, RandomSource.create(0L), direction -> false);

        return mesh.immutableCopy();
    }

    public static void register() {
        WorldRenderCallback.AFTER_LEVEL.register(QuarryMonitorPreviewRenderer::renderInWorld);
    }
}
