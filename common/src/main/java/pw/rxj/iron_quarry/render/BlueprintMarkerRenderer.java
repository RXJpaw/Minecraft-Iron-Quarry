package pw.rxj.iron_quarry.render;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import pw.rxj.iron_quarry.Main;

import java.util.ArrayList;
import java.util.List;

public abstract class BlueprintMarkerRenderer {
    public static final Identifier MARKERS_TEXTURE = Identifier.fromNamespaceAndPath(Main.MOD_ID, "textures/gui/blueprint_markers.png");
    public static final int MARKERS_TEXTURE_SIZE = 64;

    private static final Vec2 FIRST_POS_UV = new Vec2(0, 0);
    private static final Vec2 SECOND_POS_UV = new Vec2(13, 0);
    private static final Vec2 SAME_POS_UV = new Vec2(26, 0);
    private static final Vec2 BLOCKED_POS_UV = new Vec2(0, 13);
    private static final Vec2 MINING_POS_UV = new Vec2(13, 13);

    protected static class ScreenPosData {
        public final ScreenPos screenPos;
        public final Vec3 worldPos;
        public final double distance;
        public final Vec2 texUV;
        public final float alpha;

        private ScreenPosData(ScreenPos screenPos, Vec3 worldPos, double distance, Vec2 texUV, float alpha) {
            this.screenPos = screenPos;
            this.worldPos = worldPos;
            this.distance = distance;
            this.texUV = texUV;
            this.alpha = alpha;
        }

        public ScreenPosData withAlpha(float alpha) {
            return new ScreenPosData(this.screenPos, this.worldPos, this.distance, this.texUV, alpha);
        }
    }

    protected List<ScreenPosData> screenPosList = List.of();
    protected Camera camera;
    protected Matrix4f positionMatrix;
    protected Matrix4f projectionMatrix;

    protected abstract ScreenPos project(Vec3 worldPos);

    public void clear() {
        this.screenPosList = List.of();
    }

    public void update(@Nullable BlockPos firstPos, @Nullable BlockPos secondPos, @Nullable BlockPos miningPos, @Nullable BlockPos blockedPos, Camera camera, Matrix4f positionMatrix, Matrix4f projectionMatrix) {
        this.camera = camera;
        this.positionMatrix = positionMatrix;
        this.projectionMatrix = projectionMatrix;

        List<ScreenPosData> screenPosList = new ArrayList<>();

        this.addPositionMarkers(screenPosList, firstPos, secondPos);

        if(blockedPos != null) {
            this.addQuarryMarker(screenPosList, blockedPos, BLOCKED_POS_UV);
        } else if(miningPos != null) {
            this.addQuarryMarker(screenPosList, miningPos, MINING_POS_UV);
        }

        this.screenPosList = screenPosList;
    }

    private void addPositionMarkers(List<ScreenPosData> screenPosList, @Nullable BlockPos firstPos, @Nullable BlockPos secondPos) {
        if(firstPos != null && firstPos.equals(secondPos)) {
            screenPosList.add(this.toScreenPosData(firstPos, SAME_POS_UV));
            return;
        }

        if(firstPos != null) {
            screenPosList.add(this.toScreenPosData(firstPos, FIRST_POS_UV));
        }

        if(secondPos != null) {
            screenPosList.add(this.toScreenPosData(secondPos, SECOND_POS_UV));
        }

        if(firstPos != null && secondPos != null) {
            screenPosList.sort((a, b) -> Double.compare(b.distance, a.distance));

            screenPosList.set(0, fadeBehind(screenPosList.get(0), screenPosList.get(1)));
        }
    }
    private void addQuarryMarker(List<ScreenPosData> screenPosList, BlockPos blockPos, Vec2 texUV) {
        ScreenPosData quarryMarker = this.toScreenPosData(blockPos, texUV);

        screenPosList.replaceAll(screenPosData -> fadeBehind(screenPosData, quarryMarker));
        screenPosList.add(quarryMarker);
    }
    private static ScreenPosData fadeBehind(ScreenPosData background, ScreenPosData foreground) {
        if(!foreground.screenPos.isAhead()) return background;

        float distance = (float) background.screenPos.distanceTo(foreground.screenPos);
        if(distance >= 20.0F) return background;

        float alpha = Math.clamp((distance - 4.0F) / 16.0F, 0.2F, 1.0F);

        return background.withAlpha(Math.min(background.alpha, alpha));
    }

    private ScreenPosData toScreenPosData(BlockPos blockPos, Vec2 texUV) {
        Vec3 worldPos = RenderUtil.vec3dFrom(blockPos);

        return new ScreenPosData(this.project(worldPos), worldPos, worldPos.distanceTo(this.camera.position()), texUV, 1.0F);
    }

    protected static float getScale(double distance) {
        float scale = (float) Math.max(1.0, (3.0 / Math.pow(distance, 0.6)));
        //Adjust scale for low resolution displays that default to GUI scale 1.
        if(Minecraft.getInstance().getWindow().getGuiScale() <= 1) scale *= 1.5F;

        return scale;
    }
}
