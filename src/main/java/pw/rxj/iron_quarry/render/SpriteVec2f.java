package pw.rxj.iron_quarry.render;

import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

import java.util.ArrayList;
import java.util.List;

public class SpriteVec2f {
    public final Vec3f from;
    public final Vec3f to;

    private SpriteVec2f(Vec3f from, Vec3f to) {
        this.from = from;
        this.to = to;
    }

    public static SpriteVec2f from(Vec3f from, Vec3f to) {
        return new SpriteVec2f(from, to);
    }
    public static SpriteVec2f from(Vec3d from, Vec3d to) {
        return from(new Vec3f(from), new Vec3f(to));
    }

    public Vec3f normalize() {
        Vec3f normalized = this.to.copy();
        normalized.subtract(this.from);
        normalized.normalize();

        return normalized;
    }
    public SpriteVec2f normalizeSeparate() {
        Vec3f normalizedFrom = this.from.copy();
        normalizedFrom.normalize();
        Vec3f normalizedTo = this.to.copy();
        normalizedTo.normalize();

        return SpriteVec2f.from(normalizedFrom, normalizedTo);
    }

    public SpriteVec2f swap() {
        return from(to, from);
    }

    public double innerDistance() {
        return Math.sqrt(this.innerSquaredDistance());
    }
    public double innerSquaredDistance() {
        double x = this.from.getX() - this.to.getX();
        double y = this.from.getY() - this.to.getY();
        double z = this.from.getZ() - this.to.getZ();

        return x * x + y * y + z * z;
    }
    public double distanceTo(Vec3f pos) {
        return Math.sqrt(this.squaredDistanceTo(pos));
    }
    public double squaredDistanceTo(Vec3f pos) {
        Vec3f center = this.center();

        double x = pos.getX() - center.getX();
        double y = pos.getY() - center.getY();
        double z = pos.getZ() - center.getZ();

        return x * x + y * y + z * z;
    }

    public List<SpriteVec2f> autoSplit(float roughLength) {
        return this.split((int) Math.ceil(this.innerDistance() / roughLength));
    }
    public List<SpriteVec2f> split(int parts) {
        List<SpriteVec2f> list = new ArrayList<>();

        Vec3d from = new Vec3d(this.from);
        Vec3d to = new Vec3d(this.to);

        Vec3d abs = to.subtract(from);
        Vec3d part = new Vec3d(abs.getX() / parts, abs.getY() / parts, abs.getZ() / parts);

        for (int i = 0; i < parts; i++) {
            list.add(SpriteVec2f.from(from.add(part.multiply(i)), from.add(part.multiply(i + 1))));
        }

        return list;
    }
    public Vec3f center() {
        return new Vec3f(
                (this.from.getX() + this.to.getX()) / 2,
                (this.from.getY() + this.to.getY()) / 2,
                (this.from.getZ() + this.to.getZ()) / 2
        );
    }

    public boolean isOutsideRange(double range) {
        return RenderUtil.isOutsideRange(new Vec3d(this.from), range) &&
               RenderUtil.isOutsideRange(new Vec3d(this.to), range);
    }
    public boolean isOutsideCuboid(Cuboid cuboid) {
         return cuboid.isOutside(new Vec3d(this.from)) &&
                cuboid.isOutside(new Vec3d(this.to));
    }

    @Override
    public String toString() {
        return String.format("SpriteVec{from: %s, to: %s}", this.from, this.to);
    }
}
