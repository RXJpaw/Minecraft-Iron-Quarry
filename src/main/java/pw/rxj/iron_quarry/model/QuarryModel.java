package pw.rxj.iron_quarry.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableMesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.*;
import net.minecraft.client.texture.Sprite;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
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

import java.util.List;
import java.util.function.Predicate;

@Environment(EnvType.CLIENT)
public class QuarryModel implements BlockStateModel {
    private final Sprite PARTICLE_SPRITE;
    private final Sprite QUARRY_SPRITE;
    private final Sprite IO_SPRITE;

    private final QuarryBlock quarryBlock;

    private QuarryModel(QuarryBlock quarryBlock, Sprite particleSprite, Sprite quarrySprite, Sprite ioSprite) {
        this.quarryBlock = quarryBlock;

        this.PARTICLE_SPRITE = particleSprite;
        this.QUARRY_SPRITE = quarrySprite;
        this.IO_SPRITE = ioSprite;
    }

    public static QuarryModel bake(QuarryBlock quarryBlock, Baker baker) {
        ErrorCollectingSpriteGetter spriteGetter = baker.getSpriteGetter();
        SimpleModel modelSource = () -> Registries.BLOCK.getId(quarryBlock).toString();

        return new QuarryModel(quarryBlock,
                spriteGetter.get(quarryBlock.getParticleSpriteId(), modelSource),
                spriteGetter.get(quarryBlock.getSpriteId(), modelSource),
                spriteGetter.get(IoState.getSpriteId(), modelSource));
    }

    public static void register(ModelLoadingPlugin.Context pluginContext, QuarryBlock quarryBlock) {
        pluginContext.registerBlockStateResolver(quarryBlock, context -> {
            BlockStateModel.UnbakedGrouped unbaked = new Unbaked(quarryBlock).cached();

            quarryBlock.getStateManager().getStates().forEach(state -> context.setModel(state, unbaked));
        });
    }

    @Override
    public void emitQuads(QuadEmitter emitter, BlockRenderView blockView, BlockPos pos, BlockState state, Random random, Predicate<@Nullable Direction> cullTest) {
        if(blockView.getBlockEntity(pos) instanceof QuarryBlockEntity blockEntity) {
            Mesh mesh = this.getMesh(state.get(QuarryBlock.FACING), blockEntity.Configuration);
            if(mesh != null) mesh.outputTo(emitter);
        }
    }

    public void emitItemQuads(QuadEmitter emitter, ItemStack stack) {
        if(ZUtil.getBlockOrItem(stack) instanceof QuarryBlock quarryBlock) {
            Mesh mesh = this.getMesh(Direction.NORTH, quarryBlock.getMachineConfiguration(stack));
            if(mesh != null) mesh.outputTo(emitter);
        }
    }

    @Override
    public void addParts(Random random, List<BlockModelPart> parts) {

    }

    @Override
    public Sprite particleSprite() {
        return PARTICLE_SPRITE;
    }

    private record Unbaked(QuarryBlock quarryBlock) implements BlockStateModel.Unbaked {
        @Override
        public void resolve(ResolvableModel.Resolver resolver) {

        }
        @Override
        public BlockStateModel bake(Baker baker) {
            return QuarryModel.bake(this.quarryBlock, baker);
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
