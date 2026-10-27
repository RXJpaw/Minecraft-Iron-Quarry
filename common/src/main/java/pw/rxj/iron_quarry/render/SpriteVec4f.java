package pw.rxj.iron_quarry.render;

import org.joml.Vector3f;

public class SpriteVec4f {
    public final Vector3f tl;
    public final Vector3f bl;
    public final Vector3f br;
    public final Vector3f tr;

    private SpriteVec4f(Vector3f tl, Vector3f bl, Vector3f br, Vector3f tr) {
        this.tl = tl;
        this.bl = bl;
        this.br = br;
        this.tr = tr;
    }

    public static SpriteVec4f from(Vector3f tl, Vector3f bl, Vector3f br, Vector3f tr) {
        return new SpriteVec4f(tl, bl, br, tr);
    }

    @Override
    public String toString() {
        return String.format("SpriteVec{tl: %s, bl: %s, br: %s, tr: %s}", this.tl, this.bl, this.br, this.tr);
    }
}
