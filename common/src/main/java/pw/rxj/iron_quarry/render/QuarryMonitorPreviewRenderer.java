package pw.rxj.iron_quarry.render;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
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

import java.util.ArrayList;
import java.util.List;

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

        final List<RenderUtil.PreviewQuad> quads;
        final boolean coversRealBlock;

        if(ZUtil.getBlockOrItem(quarryStack) instanceof QuarryBlock quarryBlock) {
            if(!(blockStateModelSet.get(quarryBlock.defaultBlockState()) instanceof QuarryModel quarryModel)) return;

            BlockState blockState = world.getBlockState(quarryPos);
            coversRealBlock = blockState.getBlock() instanceof QuarryBlock && depth <= maxDepth && enlargement <= 1.0F;
            Direction frontDirection = coversRealBlock ? blockState.getValue(QuarryBlock.FACING) : quarryMonitorItem.getCachedFacing(quarryMonitorStack);

            quads = quarryModel.getPreviewQuads(quarryStack, frontDirection);
        } else {
            coversRealBlock = false;
            quads = getMissingQuarryQuads(blockStateModelSet);
        }

        if(quads.isEmpty()) return;

        float approach = coversRealBlock ? (float) (1.0 - 0.001 - depth * 5.0E-6) : 1.0F;
        float scale = depthScale * enlargement * approach;
        Vec3 modelPos = previewPos.scale(approach);

        PoseStack matrices = new PoseStack();
        matrices.mulPose(cameraRenderState.viewRotationMatrix);
        matrices.translate(modelPos.x, modelPos.y, modelPos.z);
        matrices.scale(scale, scale, scale);
        matrices.translate(-0.5F, -0.5F, -0.5F);

        RenderUtil.drawPreviewMesh(matrices, quads, world.cardinalLighting(), PREVIEW_ALPHA, coversRealBlock);
    }

    private static List<RenderUtil.PreviewQuad> getMissingQuarryQuads(BlockStateModelSet blockStateModelSet) {
        BlockState blockState = MISSING_QUARRY_BLOCK.defaultBlockState();
        List<BlockStateModelPart> parts = new ArrayList<>();
        List<RenderUtil.PreviewQuad> quads = new ArrayList<>();

        blockStateModelSet.get(blockState).collectParts(RandomSource.create(0L), parts);

        for (BlockStateModelPart part : parts) {
            for (Direction direction : Direction.values()) {
                part.getQuads(direction).forEach(quad -> quads.add(RenderUtil.PreviewQuad.of(quad)));
            }

            part.getQuads(null).forEach(quad -> quads.add(RenderUtil.PreviewQuad.of(quad)));
        }

        return quads;
    }

    public static void register() {
        WorldRenderCallback.AFTER_LEVEL.register(QuarryMonitorPreviewRenderer::renderInWorld);
    }
}
