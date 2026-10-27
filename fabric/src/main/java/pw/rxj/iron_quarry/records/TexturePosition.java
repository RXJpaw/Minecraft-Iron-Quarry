package pw.rxj.iron_quarry.records;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;

public record TexturePosition(int u, int v, int width, int height) {
    public int minU() {
        return u;
    }
    public float minU(int spriteSize) {
        return (float) u / spriteSize;
    }

    public int maxU(){
        return u + width;
    }
    public float maxU(int spriteSize){
        return (float) (u + width) / spriteSize;
    }

    public int minV() {
        return v;
    }
    public float minV(int spriteSize) {
        return (float) v / spriteSize;
    }

    public int maxV(){
        return v + width;
    }
    public float maxV(int spriteSize){
        return (float) (v + width) / spriteSize;
    }

    public void applySprite(QuadEmitter emitter, int spriteIndex, int spriteSize) {
        emitter.uv(0, (float) (u) / spriteSize,         (float) (v) / spriteSize);          //top left
        emitter.uv(1, (float) (u) / spriteSize,         (float) (v + height) / spriteSize); //bottom left
        emitter.uv(2, (float) (u + width) / spriteSize, (float) (v + height) / spriteSize); //bottom right
        emitter.uv(3, (float) (u + width) / spriteSize, (float) (v) / spriteSize);          //top right
    }
}
