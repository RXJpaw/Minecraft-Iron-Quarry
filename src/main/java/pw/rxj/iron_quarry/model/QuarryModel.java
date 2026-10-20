package pw.rxj.iron_quarry.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.renderer.v1.Renderer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableMesh;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.MaterialBaker;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import pw.rxj.iron_quarry.block.QuarryBlock;
import pw.rxj.iron_quarry.blockentity.QuarryBlockEntity;
import pw.rxj.iron_quarry.types.Face;
import pw.rxj.iron_quarry.types.IoState;
import pw.rxj.iron_quarry.util.MachineConfiguration;
import pw.rxj.iron_quarry.util.ZUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Environment(EnvType.CLIENT)
public class QuarryModel implements BlockStateModel {
    private final Material.Baked PARTICLE_SPRITE;
    private final Material.Baked QUARRY_SPRITE;
    private final Material.Baked IO_SPRITE;

    private final QuarryBlock quarryBlock;

    private QuarryModel(QuarryBlock quarryBlock, Material.Baked particleSprite, Material.Baked quarrySprite, Material.Baked ioSprite) {
        this.quarryBlock = quarryBlock;

        this.PARTICLE_SPRITE = particleSprite;
        this.QUARRY_SPRITE = quarrySprite;
        this.IO_SPRITE = ioSprite;
    }

    public static QuarryModel bake(QuarryBlock quarryBlock, ModelBaker baker) {
        MaterialBaker materialBaker = baker.materials();
        ModelDebugName modelSource = () -> BuiltInRegistries.BLOCK.getKey(quarryBlock).toString();

        return new QuarryModel(quarryBlock,
                bakeSprite(materialBaker, quarryBlock.getParticleSpriteId(), modelSource),
                bakeSprite(materialBaker, quarryBlock.getSpriteId(), modelSource),
                bakeSprite(materialBaker, IoState.getSpriteId(), modelSource));
    }
    private static Material.Baked bakeSprite(MaterialBaker materialBaker, SpriteId spriteId, ModelDebugName modelSource) {
        return materialBaker.get(new Material(spriteId.texture()), modelSource);
    }

    public static void register(ModelLoadingPlugin.Context pluginContext, QuarryBlock quarryBlock) {
        pluginContext.registerBlockStateResolver(quarryBlock, context -> {
            BlockStateModel.UnbakedRoot unbaked = new pw.rxj.iron_quarry.model.QuarryModel.Unbaked(quarryBlock).asRoot();

            quarryBlock.getStateDefinition().getPossibleStates().forEach(state -> context.setModel(state, unbaked));
        });
    }

    @Override
    public void emitQuads(QuadEmitter emitter, BlockAndTintGetter blockView, BlockPos pos, BlockState state, RandomSource random, Predicate<Direction> cullTest) {
        if(blockView.getBlockEntity(pos) instanceof QuarryBlockEntity blockEntity) {
            Mesh mesh = this.getMesh(state.getValue(QuarryBlock.FACING), blockEntity.Configuration);
            if(mesh != null) mesh.outputTo(emitter);
        }
    }

    public List<BakedQuad> getItemQuads(ItemStack stack) {
        List<BakedQuad> quads = new ArrayList<>();

        if(ZUtil.getBlockOrItem(stack) instanceof QuarryBlock quarryBlock) {
            Mesh mesh = this.getMesh(Direction.NORTH, quarryBlock.getMachineConfiguration(stack));
            if(mesh != null) mesh.forEach(quad -> quads.add(quad.toBakedQuad(QUARRY_SPRITE.sprite())));
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

    private record Unbaked(QuarryBlock quarryBlock) implements BlockStateModel.Unbaked {
        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {

        }
        @Override
        public BlockStateModel bake(ModelBaker baker) {
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
            emitter.materialBake(QUARRY_SPRITE, MutableQuadView.BAKE_NORMALIZED);
            emitter.color(-1, -1, -1, -1);
            emitter.emit();

            if(!isOpen) continue;

            emitter.square(direction, 0.25F, 0.25F, 0.75F, 0.75F, 0.0F);
            IoState.getTexturePosition(configuration.getIoState(face)).applySprite(emitter, 0, 256);
            emitter.materialBake(IO_SPRITE, MutableQuadView.BAKE_NORMALIZED);
            emitter.color(-1, -1, -1, -1);
            emitter.emit();
        }

        return mesh.immutableCopy();
    }
}
