package pw.rxj.iron_quarry.render;

import net.minecraft.util.math.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Cuboid {
    private boolean fullblockAdjusted = false;

    public Vec3d lowPos;
    public Vec3d highPos;

    private Cuboid(Vec3d pos1, Vec3d pos2, boolean process) {
        if(process) {
            this.lowPos = new Vec3d(
                    Math.min(pos1.getX(), pos2.getX()),
                    Math.min(pos1.getY(), pos2.getY()),
                    Math.min(pos1.getZ(), pos2.getZ())
            );
            this.highPos = new Vec3d(
                    Math.max(pos1.getX(), pos2.getX()),
                    Math.max(pos1.getY(), pos2.getY()),
                    Math.max(pos1.getZ(), pos2.getZ())
            );
        } else {
            this.lowPos = pos1;
            this.highPos = pos2;
        }
    }
    public static Cuboid from(Vec3d pos1, Vec3d pos2) {
        return new Cuboid(pos1, pos2, true);
    }
    private static Cuboid fromProcessed(Vec3d lowPos, Vec3d highPos) {
        return new Cuboid(lowPos, highPos, false);
    }
    public static Cuboid from(double allSame1, double allSame2) {
        Vec3d pos1 = new Vec3d(allSame1, allSame1, allSame1);
        Vec3d pos2 = new Vec3d(allSame2, allSame2, allSame2);

        return from(pos1, pos2);
    }
    public static Cuboid from(BlockPos blockPos1, BlockPos blockPos2) {
        Vec3d pos1 = new Vec3d(blockPos1.getX(), blockPos1.getY(), blockPos1.getZ());
        Vec3d pos2 = new Vec3d(blockPos2.getX(), blockPos2.getY(), blockPos2.getZ());

        return from(pos1, pos2);
    }

    public Cuboid subtract(double allSame) {
        return this.subtract(new Vec3d(allSame, allSame, allSame));
    }
    public Cuboid subtract(Vec3d vector) {
        return Cuboid.fromProcessed(this.lowPos.subtract(vector), this.highPos.subtract(vector));
    }
    public Cuboid add(double allSame) {
        return this.add(new Vec3d(allSame, allSame, allSame));
    }
    public Cuboid add(Vec3d vector) {
        return Cuboid.fromProcessed(this.lowPos.add(vector), this.highPos.add(vector));
    }
    public Cuboid inflate(double offset) {
        return this.inflate(new Vec3d(offset, offset, offset));
    }
    public Cuboid inflate(Vec3d offset) {
        return Cuboid.fromProcessed(this.lowPos.subtract(offset), this.highPos.add(offset));
    }
    public Cuboid deflate(double offset) {
        return this.deflate(new Vec3d(offset, offset, offset));
    }
    public Cuboid deflate(Vec3d offset) {
        return Cuboid.fromProcessed(this.lowPos.add(offset), this.highPos.subtract(offset));
    }

    public Vec3d abs() {
        return this.highPos.subtract(this.lowPos);
    }

    public Cuboid fullblock() {
        if(this.isFullblockAdjusted()) {
            throw new IllegalStateException("Cuboid is already fullblock adjusted.");
        }

        Cuboid cuboid = Cuboid.fromProcessed(this.lowPos, this.highPos.add(1, 1, 1));
        cuboid.setFullblockAdjusted();

        return cuboid;
    }
    public boolean isFullblockAdjusted() {
        return this.fullblockAdjusted;
    }
    private void setFullblockAdjusted() {
        this.fullblockAdjusted = true;
    }

    public Cuboid limitInside(Cuboid other) {
        Vec3d lowPos = new Vec3d(
                Math.min(Math.max(this.lowPos.x, other.lowPos.x), other.highPos.x),
                Math.min(Math.max(this.lowPos.y, other.lowPos.y), other.highPos.x),
                Math.min(Math.max(this.lowPos.z, other.lowPos.z), other.highPos.x)
        );
        Vec3d highPos = new Vec3d(
                Math.min(Math.max(this.highPos.x, other.lowPos.x), other.highPos.x),
                Math.min(Math.max(this.highPos.y, other.lowPos.y), other.highPos.x),
                Math.min(Math.max(this.highPos.z, other.lowPos.z), other.highPos.x)
        );

        return Cuboid.fromProcessed(lowPos, highPos);
    }

    public boolean isFlat() {
        return this.lowPos.x == this.highPos.x ||
               this.lowPos.y == this.highPos.y ||
               this.lowPos.z == this.highPos.z;
    }
    public boolean isOutside(Vec3d pos) {
        return pos.x <  this.lowPos.x || pos.y <  this.lowPos.y || pos.z <  this.lowPos.z ||
               pos.x > this.highPos.x || pos.y > this.highPos.y || pos.z > this.highPos.z;
    }
    public boolean isPointInside(Vec2f flatPos) {
        return flatPos.x >= this.lowPos.x && flatPos.y >= this.lowPos.z && flatPos.x < this.highPos.x && flatPos.y < this.highPos.z;
    }

    public List<SpriteVec2f> getLines() {
        Vec3d abs = this.abs();

        List<SpriteVec2f> lines = new ArrayList<>();

        lines.add(SpriteVec2f.from(this.lowPos, this.lowPos.add(abs.x, 0, 0)));
        lines.add(SpriteVec2f.from(this.lowPos, this.lowPos.add(0, abs.y, 0)));
        lines.add(SpriteVec2f.from(this.lowPos, this.lowPos.add(0, 0, abs.z)));

        lines.add(SpriteVec2f.from(this.lowPos.add(0, 0, abs.z), this.lowPos.add(abs.x, 0, abs.z)));
        lines.add(SpriteVec2f.from(this.lowPos.add(0, 0, abs.z), this.lowPos.add(0, abs.y, abs.z)));
        lines.add(SpriteVec2f.from(this.lowPos.add(0, abs.y, 0), this.lowPos.add(0, abs.y, abs.z)));

        lines.add(SpriteVec2f.from(this.highPos, this.highPos.subtract(abs.x, 0, 0)));
        lines.add(SpriteVec2f.from(this.highPos, this.highPos.subtract(0, abs.y, 0)));
        lines.add(SpriteVec2f.from(this.highPos, this.highPos.subtract(0, 0, abs.z)));

        lines.add(SpriteVec2f.from(this.highPos.subtract(0, 0, abs.z), this.highPos.subtract(abs.x, 0, abs.z)));
        lines.add(SpriteVec2f.from(this.highPos.subtract(0, 0, abs.z), this.highPos.subtract(0, abs.y, abs.z)));
        lines.add(SpriteVec2f.from(this.highPos.subtract(0, abs.y, 0), this.highPos.subtract(0, abs.y, abs.z)));

        return lines;
    }
    public SpriteVec4f getAnchors(Direction direction) {
        Vec3f anchor_tl = null;
        Vec3f anchor_bl = null;
        Vec3f anchor_br = null;
        Vec3f anchor_tr = null;

        float minX = (float) this.lowPos.x;
        float minY = (float) this.lowPos.y;
        float minZ = (float) this.lowPos.z;

        float maxX = (float) this.highPos.x;
        float maxY = (float) this.highPos.y;
        float maxZ = (float) this.highPos.z;

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

        return SpriteVec4f.from(anchor_tl, anchor_bl, anchor_br, anchor_tr);
    }
    public HashMap<Direction, Boolean> matchingDirections(Cuboid suspect) {
        HashMap<Direction, Boolean> directions = new HashMap<>();
        directions.put(Direction.WEST, this.lowPos.x == suspect.lowPos.x);
        directions.put(Direction.DOWN, this.lowPos.y == suspect.lowPos.y);
        directions.put(Direction.NORTH, this.lowPos.z == suspect.lowPos.z);
        directions.put(Direction.EAST, this.highPos.x == suspect.highPos.x);
        directions.put(Direction.UP, this.highPos.y == suspect.highPos.y);
        directions.put(Direction.SOUTH, this.highPos.z == suspect.highPos.z);

        return directions;
    }
}
