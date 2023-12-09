package pw.rxj.iron_quarry.model;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.blockentity.QuarryBlockEntity;
import pw.rxj.iron_quarry.block.QuarryBlock;
import pw.rxj.iron_quarry.types.Face;
import pw.rxj.iron_quarry.types.IoState;
import pw.rxj.iron_quarry.util.MachineConfiguration;
import pw.rxj.iron_quarry.util.ZUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class QuarryModel implements ComplexModel {
    private final SpriteIdentifier PARTICLE_SPRITE_ID;
    private final SpriteIdentifier QUARRY_SPRITE_ID;
    private final SpriteIdentifier IO_SPRITE_ID;
    private Sprite PARTICLE_SPRITE;
    private Sprite QUARRY_SPRITE;
    private Sprite IO_SPRITE;

    private final Identifier modelSource;
    private final QuarryBlock quarryBlock;

    private QuarryModel(QuarryBlock quarryBlock) {
        this.modelSource = Registry.BLOCK.getId(quarryBlock);
        this.quarryBlock = quarryBlock;

        this.PARTICLE_SPRITE_ID = quarryBlock.getParticleSpriteId();
        this.QUARRY_SPRITE_ID = quarryBlock.getSpriteId();
        this.IO_SPRITE_ID = IoState.getSpriteId();
    }
    protected static QuarryModel of(QuarryBlock quarryBlock) {
        return new QuarryModel(quarryBlock);
    }

    @Override
    public Identifier getModelSource() {
        return this.modelSource;
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
        if(blockView.getBlockEntity(pos) instanceof QuarryBlockEntity blockEntity) {
            Mesh mesh = this.getMesh(state.get(QuarryBlock.FACING), blockEntity.Configuration);

            context.meshConsumer().accept(mesh);
        }
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
        if(ZUtil.getBlockOrItem(stack) instanceof QuarryBlock quarryBlock1) {
            Mesh mesh = this.getMesh(Direction.NORTH, quarryBlock1.getMachineConfiguration(stack));

            context.meshConsumer().accept(mesh);
        }

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
    public boolean isBuiltin() {
        return false;
    }

    @Override
    public Sprite getParticleSprite() {
        return PARTICLE_SPRITE;
    }

    @Override
    public ModelTransformation getTransformation() {
        return ModelHelper.MODEL_TRANSFORM_BLOCK;
    }

    @Override
    public ModelOverrideList getOverrides() {
        return ModelOverrideList.EMPTY;
    }

    @Override
    public Collection<Identifier> getModelDependencies() {
        return Collections.emptyList();
    }

    @Override
    public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
        return List.of(PARTICLE_SPRITE_ID, QUARRY_SPRITE_ID, IO_SPRITE_ID);
    }

    @Nullable
    @Override
    public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
        PARTICLE_SPRITE = textureGetter.apply(PARTICLE_SPRITE_ID);
        QUARRY_SPRITE = textureGetter.apply(QUARRY_SPRITE_ID);
        IO_SPRITE = textureGetter.apply(IO_SPRITE_ID);

        return this;
    }

    private Mesh getMesh(Direction frontDirection, MachineConfiguration configuration){
        Renderer renderer = RendererAccess.INSTANCE.getRenderer();
        if(renderer == null) return null;

        MeshBuilder builder = renderer.meshBuilder();
        QuadEmitter emitter = builder.getEmitter();

        for(Direction direction : Direction.values()) {
            Face face = Face.from(direction, frontDirection);
            if(face == null) continue;

            Boolean isOpen = !configuration.getIoState(face).equals(IoState.BLOCKED);

            emitter.square(direction, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F);
            this.quarryBlock.getTexturePosition(face, isOpen).applySprite(emitter, 0, 256);
            emitter.spriteBake(0, QUARRY_SPRITE, MutableQuadView.BAKE_NORMALIZED);
            emitter.spriteColor(0, -1, -1, -1, -1);
            emitter.emit();

            if(!isOpen) continue;

            emitter.square(direction, 0.25F, 0.25F, 0.75F, 0.75F, 0.0F);
            IoState.getTexturePosition(configuration.getIoState(face)).applySprite(emitter, 0, 256);
            emitter.spriteBake(0, IO_SPRITE, MutableQuadView.BAKE_NORMALIZED);
            emitter.spriteColor(0, -1, -1, -1, -1);
            emitter.emit();
        }

        return builder.build();
    }
}
