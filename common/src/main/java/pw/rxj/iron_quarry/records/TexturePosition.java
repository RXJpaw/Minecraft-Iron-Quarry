package pw.rxj.iron_quarry.records;

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
}
