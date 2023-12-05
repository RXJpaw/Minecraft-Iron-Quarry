package pw.rxj.iron_quarry.records;

import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;

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
        emitter.sprite(0, spriteIndex, (float) (u) / spriteSize,         (float) (v) / spriteSize);          //top left
        emitter.sprite(1, spriteIndex, (float) (u) / spriteSize,         (float) (v + height) / spriteSize); //bottom left
        emitter.sprite(2, spriteIndex, (float) (u + width) / spriteSize, (float) (v + height) / spriteSize); //bottom right
        emitter.sprite(3, spriteIndex, (float) (u + width) / spriteSize, (float) (v) / spriteSize);          //top right
    }
}
