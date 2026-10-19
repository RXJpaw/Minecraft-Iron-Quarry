package pw.rxj.iron_quarry.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableMesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.block.QuarryBlock;
import pw.rxj.iron_quarry.blockentity.QuarryBlockEntity;
import pw.rxj.iron_quarry.types.Face;
import pw.rxj.iron_quarry.types.IoState;
import pw.rxj.iron_quarry.util.MachineConfiguration;
import pw.rxj.iron_quarry.util.ZUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class QuarryModel implements BakedModel, FabricBakedModel {
    private final SpriteIdentifier PARTICLE_SPRITE_ID;
    private final SpriteIdentifier QUARRY_SPRITE_ID;
    private final SpriteIdentifier IO_SPRITE_ID;
    private Sprite PARTICLE_SPRITE;
    private Sprite QUARRY_SPRITE;
    private Sprite IO_SPRITE;

    private final Identifier modelSource;
    private final QuarryBlock quarryBlock;

    private QuarryModel(QuarryBlock quarryBlock) {
        this.modelSource = Registries.BLOCK.getId(quarryBlock);
        this.quarryBlock = quarryBlock;

        this.PARTICLE_SPRITE_ID = quarryBlock.getParticleSpriteId();
        this.QUARRY_SPRITE_ID = quarryBlock.getSpriteId();
        this.IO_SPRITE_ID = IoState.getSpriteId();
    }
    protected static ComplexModel of(QuarryBlock quarryBlock) {
        return new Unbaked(new QuarryModel(quarryBlock));
    }

    public Identifier getModelSource() {
        return this.modelSource;
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(QuadEmitter emitter, BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, Predicate<@Nullable Direction> cullTest) {
        if(blockView.getBlockEntity(pos) instanceof QuarryBlockEntity blockEntity) {
            Mesh mesh = this.getMesh(state.get(QuarryBlock.FACING), blockEntity.Configuration);
            if(mesh != null) mesh.outputTo(emitter);
        }
    }

    public BakedModel forItem(ItemStack stack) {
        if(ZUtil.getBlockOrItem(stack) instanceof QuarryBlock quarryBlock) {
            return new ItemVariant(this, this.getMesh(Direction.NORTH, quarryBlock.getMachineConfiguration(stack)));
        }

        return this;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
        return Collections.emptyList();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean hasDepth() {
        return false;
    }

    @Override
    public boolean isSideLit() {
        return true;
    }

    @Override
    public Sprite getParticleSprite() {
        return PARTICLE_SPRITE;
    }

    @Override
    public ModelTransformation getTransformation() {
        return ModelHelper.MODEL_TRANSFORM_BLOCK;
    }

    private record Unbaked(QuarryModel model) implements ComplexModel {
        @Override
        public Identifier getModelSource() {
            return this.model.getModelSource();
        }
        @Override
        public void resolve(ResolvableModel.Resolver resolver) {

        }
        @Override
        public BakedModel bake(ModelTextures textures, Baker baker, ModelBakeSettings settings, boolean ambientOcclusion, boolean isSideLit, ModelTransformation transformation) {
            return this.model.bakeSprites(baker);
        }
    }

    private QuarryModel bakeSprites(Baker baker) {
        PARTICLE_SPRITE = baker.getSpriteGetter().get(PARTICLE_SPRITE_ID);
        QUARRY_SPRITE = baker.getSpriteGetter().get(QUARRY_SPRITE_ID);
        IO_SPRITE = baker.getSpriteGetter().get(IO_SPRITE_ID);

        return this;
    }

    private record ItemVariant(QuarryModel parent, @Nullable Mesh mesh) implements BakedModel, FabricBakedModel {
        @Override
        public boolean isVanillaAdapter() {
            return false;
        }
        @Override
        public void emitItemQuads(QuadEmitter emitter, Supplier<Random> randomSupplier) {
            if(this.mesh != null) this.mesh.outputTo(emitter);
        }
        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
            return Collections.emptyList();
        }
        @Override
        public boolean useAmbientOcclusion() {
            return this.parent.useAmbientOcclusion();
        }
        @Override
        public boolean hasDepth() {
            return this.parent.hasDepth();
        }
        @Override
        public boolean isSideLit() {
            return this.parent.isSideLit();
        }
        @Override
        public Sprite getParticleSprite() {
            return this.parent.getParticleSprite();
        }
        @Override
        public ModelTransformation getTransformation() {
            return this.parent.getTransformation();
        }
    }

    private Mesh getMesh(Direction frontDirection, MachineConfiguration configuration){
        MutableMesh mesh = Renderer.get().mutableMesh();
        QuadEmitter emitter = mesh.emitter();

        for(Direction direction : Direction.values()) {
            Face face = Face.from(direction, frontDirection);
            if(face == null) continue;

            Boolean isOpen = !configuration.getIoState(face).equals(IoState.BLOCKED);

            emitter.square(direction, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F);
            this.quarryBlock.getTexturePosition(face, isOpen).applySprite(emitter, 0, 256);
            emitter.spriteBake(QUARRY_SPRITE, MutableQuadView.BAKE_NORMALIZED);
            emitter.color(-1, -1, -1, -1);
            emitter.emit();

            if(!isOpen) continue;

            emitter.square(direction, 0.25F, 0.25F, 0.75F, 0.75F, 0.0F);
            IoState.getTexturePosition(configuration.getIoState(face)).applySprite(emitter, 0, 256);
            emitter.spriteBake(IO_SPRITE, MutableQuadView.BAKE_NORMALIZED);
            emitter.color(-1, -1, -1, -1);
            emitter.emit();
        }

        return mesh.immutableCopy();
    }
}
