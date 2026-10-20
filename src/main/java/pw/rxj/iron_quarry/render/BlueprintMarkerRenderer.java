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
    public static final Identifier BLUEPRINT_POSITIONS_TEXTURE = Identifier.fromNamespaceAndPath(Main.MOD_ID, "textures/gui/blueprint_positions.png");

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

    public void update(@Nullable BlockPos firstPos, @Nullable BlockPos secondPos, Camera camera, Matrix4f positionMatrix, Matrix4f projectionMatrix) {
        this.camera = camera;
        this.positionMatrix = positionMatrix;
        this.projectionMatrix = projectionMatrix;

        List<ScreenPosData> screenPosList = new ArrayList<>();

        if(firstPos != null) {
            screenPosList.add(this.toScreenPosData(firstPos, new Vec2(0, 0)));
        }

        if(secondPos != null) {
            screenPosList.add(this.toScreenPosData(secondPos, new Vec2(13, 0)));
        }

        if(firstPos != null && secondPos != null) {
            screenPosList.sort((a, b) -> Double.compare(b.distance, a.distance));

            ScreenPosData background = screenPosList.get(0);
            ScreenPosData foreground = screenPosList.get(1);

            float distance = (float) background.screenPos.distanceTo(foreground.screenPos);

            if(foreground.screenPos.isAhead() && distance < 20.0F) {
                float alpha = Math.clamp((distance - 4.0F) / 16.0F, 0.2F, 1.0F);

                screenPosList.set(0, background.withAlpha(alpha));
            }
        }

        this.screenPosList = screenPosList;
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
