package pw.rxj.iron_quarry.model;

import com.mojang.blaze3d.platform.Transparency;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.MaterialBaker;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import pw.rxj.iron_quarry.block.QuarryBlock;
import pw.rxj.iron_quarry.records.TexturePosition;
import pw.rxj.iron_quarry.render.RenderUtil;
import pw.rxj.iron_quarry.types.Face;
import pw.rxj.iron_quarry.types.IoState;
import pw.rxj.iron_quarry.util.MachineConfiguration;
import pw.rxj.iron_quarry.util.ZUtil;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class QuarryModel implements BlockStateModel {
    private static final float CULL_FACE_EPSILON = 0.00001F;

    protected final Material.Baked PARTICLE_SPRITE;
    protected final Material.Baked QUARRY_SPRITE;
    protected final Material.Baked IO_SPRITE;

    protected final QuarryBlock quarryBlock;

    protected QuarryModel(QuarryBlock quarryBlock, ModelBaker baker) {
        MaterialBaker materialBaker = baker.materials();
        ModelDebugName modelSource = () -> BuiltInRegistries.BLOCK.getKey(quarryBlock).toString();

        this.quarryBlock = quarryBlock;

        this.PARTICLE_SPRITE = bakeSprite(materialBaker, quarryBlock.getParticleSpriteId(), modelSource);
        this.QUARRY_SPRITE = bakeSprite(materialBaker, quarryBlock.getSpriteId(), modelSource);
        this.IO_SPRITE = bakeSprite(materialBaker, IoState.getSpriteId(), modelSource);
    }

    public static QuarryModel bake(QuarryBlock quarryBlock, ModelBaker baker) {
        return new QuarryModel(quarryBlock, baker);
    }

    private static Material.Baked bakeSprite(MaterialBaker materialBaker, SpriteId spriteId, ModelDebugName modelSource) {
        return materialBaker.get(new Material(spriteId.texture()), modelSource);
    }

    public List<BakedQuad> getItemQuads(ItemStack stack) {
        List<BakedQuad> quads = new ArrayList<>();

        if(ZUtil.getBlockOrItem(stack) instanceof QuarryBlock quarryBlock) {
            this.getQuads(Direction.NORTH, quarryBlock.getMachineConfiguration(stack), true).forEach(quad -> quads.add(quad.toBakedQuad()));
        }

        return quads;
    }

    public List<RenderUtil.PreviewQuad> getPreviewQuads(ItemStack stack, Direction frontDirection) {
        List<RenderUtil.PreviewQuad> quads = new ArrayList<>();

        if(ZUtil.getBlockOrItem(stack) instanceof QuarryBlock quarryBlock) {
            quads.addAll(this.getQuads(frontDirection, quarryBlock.getMachineConfiguration(stack), false));
        }

        return quads;
    }

    @Override
    public void collectParts(RandomSource random, List<BlockStateModelPart> parts) {

    }

    @Override
    public Material.Baked particleMaterial() {
        return PARTICLE_SPRITE;
    }

    @Override
    public int materialFlags() {
        return 0;
    }

    protected List<Quad> getQuads(Direction frontDirection, MachineConfiguration configuration, boolean withBacking){
        List<Quad> quads = new ArrayList<>();

        for(Direction direction : Direction.values()) {
            Face face = Face.from(direction, frontDirection);
            if(face == null) continue;

            Boolean isOpen = !configuration.getIoState(face).equals(IoState.BLOCKED);

            quads.add(Quad.of(direction, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, this.quarryBlock.getTexturePosition(face, isOpen), QUARRY_SPRITE, -1));

            if(!isOpen) continue;

            quads.add(Quad.of(direction, 0.25F, 0.25F, 0.75F, 0.75F, 0.0F, IoState.getTexturePosition(configuration.getIoState(face)), IO_SPRITE, -1));

            if(!withBacking) continue;

            // black backing so sodium's UV inset doesn't reveal the world around the IO overlay
            quads.add(Quad.of(direction, 0.0F, 0.0F, 1.0F, 1.0F, 0.0625F, IoState.getTexturePosition(configuration.getIoState(face)), IO_SPRITE, 0xFF000000));
        }

        return quads;
    }

    public static class Quad implements RenderUtil.PreviewQuad {
        public final Direction direction;
        public final float left;
        public final float bottom;
        public final float right;
        public final float top;
        public final float depth;
        public final TexturePosition texturePosition;
        public final Material.Baked material;
        public final int color;
        private final Vector3f[] positions = new Vector3f[4];
        private final float[] us = new float[4];
        private final float[] vs = new float[4];

        private Quad(Direction direction, float left, float bottom, float right, float top, float depth, TexturePosition texturePosition, Material.Baked material, int color) {
            this.direction = direction;
            this.left = left;
            this.bottom = bottom;
            this.right = right;
            this.top = top;
            this.depth = depth;
            this.texturePosition = texturePosition;
            this.material = material;
            this.color = color;

            this.computePositions();
            this.computeUVs(256);
        }
        public static Quad of(Direction direction, float left, float bottom, float right, float top, float depth, TexturePosition texturePosition, Material.Baked material, int color) {
            return new Quad(direction, left, bottom, right, top, depth, texturePosition, material, color);
        }

        private void computePositions() {
            float left = this.left;
            float bottom = this.bottom;
            float right = this.right;
            float top = this.top;
            float depth = Math.abs(this.depth) < CULL_FACE_EPSILON ? 0.0F : this.depth;

            switch (this.direction) {
                case UP, DOWN -> {
                    if(this.direction == Direction.UP) {
                        depth = 1.0F - depth;
                        top = 1.0F - top;
                        bottom = 1.0F - bottom;
                    }

                    this.positions[0] = new Vector3f(left, depth, top);
                    this.positions[1] = new Vector3f(left, depth, bottom);
                    this.positions[2] = new Vector3f(right, depth, bottom);
                    this.positions[3] = new Vector3f(right, depth, top);
                }
                case EAST, WEST -> {
                    if(this.direction == Direction.EAST) {
                        depth = 1.0F - depth;
                        left = 1.0F - left;
                        right = 1.0F - right;
                    }

                    this.positions[0] = new Vector3f(depth, top, left);
                    this.positions[1] = new Vector3f(depth, bottom, left);
                    this.positions[2] = new Vector3f(depth, bottom, right);
                    this.positions[3] = new Vector3f(depth, top, right);
                }
                case SOUTH, NORTH -> {
                    if(this.direction == Direction.SOUTH) {
                        depth = 1.0F - depth;
                        left = 1.0F - left;
                        right = 1.0F - right;
                    }

                    this.positions[0] = new Vector3f(1.0F - left, top, depth);
                    this.positions[1] = new Vector3f(1.0F - left, bottom, depth);
                    this.positions[2] = new Vector3f(1.0F - right, bottom, depth);
                    this.positions[3] = new Vector3f(1.0F - right, top, depth);
                }
            }
        }
        private void computeUVs(int spriteSize) {
            float minU = (float) this.texturePosition.u() / spriteSize;
            float minV = (float) this.texturePosition.v() / spriteSize;
            float maxU = (float) (this.texturePosition.u() + this.texturePosition.width()) / spriteSize;
            float maxV = (float) (this.texturePosition.v() + this.texturePosition.height()) / spriteSize;

            this.setUV(0, minU, minV); //top left
            this.setUV(1, minU, maxV); //bottom left
            this.setUV(2, maxU, maxV); //bottom right
            this.setUV(3, maxU, minV); //top right
        }
        private void setUV(int vertex, float u, float v) {
            this.us[vertex] = this.material.sprite().getU(u);
            this.vs[vertex] = this.material.sprite().getV(v);
        }

        public Direction cullFace() {
            return this.direction;
        }

        public BakedQuad toBakedQuad() {
            return new BakedQuad(
                    this.positions[0], this.positions[1], this.positions[2], this.positions[3],
                    UVPair.pack(this.us[0], this.vs[0]), UVPair.pack(this.us[1], this.vs[1]), UVPair.pack(this.us[2], this.vs[2]), UVPair.pack(this.us[3], this.vs[3]),
                    this.direction,
                    BakedQuad.MaterialInfo.of(this.material, this.computeTransparency(), -1, true, 0)
            );
        }
        private Transparency computeTransparency() {
            if(this.material.forceTranslucent()) return Transparency.TRANSLUCENT;

            float minU = (float) this.texturePosition.u() / 256;
            float minV = (float) this.texturePosition.v() / 256;
            float maxU = (float) (this.texturePosition.u() + this.texturePosition.width()) / 256;
            float maxV = (float) (this.texturePosition.v() + this.texturePosition.height()) / 256;

            return this.material.sprite().contents().computeTransparency(minU, minV, maxU, maxV);
        }

        @Override
        public float x(int vertex) {
            return this.positions[vertex].x();
        }
        @Override
        public float y(int vertex) {
            return this.positions[vertex].y();
        }
        @Override
        public float z(int vertex) {
            return this.positions[vertex].z();
        }
        @Override
        public float u(int vertex) {
            return this.us[vertex];
        }
        @Override
        public float v(int vertex) {
            return this.vs[vertex];
        }
        @Override
        public int color(int vertex) {
            return this.color;
        }
        @Override
        public boolean diffuseShade() {
            return true;
        }
        @Override
        public Direction lightFace() {
            return this.direction;
        }
    }
}
