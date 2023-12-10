package pw.rxj.iron_quarry.render;

import net.minecraft.util.math.Vec3f;

public class SpriteVec {
    public final Vec3f tl;
    public final Vec3f bl;
    public final Vec3f br;
    public final Vec3f tr;

    private SpriteVec(Vec3f tl, Vec3f bl, Vec3f br, Vec3f tr) {
        this.tl = tl;
        this.bl = bl;
        this.br = br;
        this.tr = tr;
    }

    public static SpriteVec from(Vec3f tl, Vec3f bl, Vec3f br, Vec3f tr) {
        return new SpriteVec(tl, bl, br, tr);
    }

    @Override
    public String toString() {
        return String.format("SpriteVec[tl: %s, bl: %s, br: %s, tr: %s]", this.tl, this.bl, this.br, this.tr);
    }
}
