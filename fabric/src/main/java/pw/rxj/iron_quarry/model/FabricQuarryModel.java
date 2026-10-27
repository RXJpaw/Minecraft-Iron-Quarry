package pw.rxj.iron_quarry.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import pw.rxj.iron_quarry.block.QuarryBlock;
import pw.rxj.iron_quarry.blockentity.QuarryBlockEntity;
import pw.rxj.iron_quarry.util.MachineConfiguration;

import java.util.function.Predicate;

@Environment(EnvType.CLIENT)
public class FabricQuarryModel extends QuarryModel {
    private FabricQuarryModel(QuarryBlock quarryBlock, ModelBaker baker) {
        super(quarryBlock, baker);
    }

    public static void register(ModelLoadingPlugin.Context pluginContext, QuarryBlock quarryBlock) {
        pluginContext.registerBlockStateResolver(quarryBlock, context -> {
            BlockStateModel.UnbakedRoot unbaked = Unbaked.of(quarryBlock).asRoot();

            quarryBlock.getStateDefinition().getPossibleStates().forEach(state -> context.setModel(state, unbaked));
        });
    }

    @Override
    public void emitQuads(QuadEmitter emitter, BlockAndTintGetter blockView, BlockPos pos, BlockState state, RandomSource random, Predicate<Direction> cullTest) {
        MachineConfiguration configuration = blockView.getBlockEntity(pos) instanceof QuarryBlockEntity blockEntity ? blockEntity.Configuration : MachineConfiguration.empty();

        Direction frontDirection = state.getOptionalValue(QuarryBlock.FACING).orElse(Direction.NORTH);

        for (Quad quad : this.getQuads(frontDirection, configuration, true)) {
            emitter.fromBakedQuad(quad.toBakedQuad());
            emitter.cullFace(quad.cullFace());
            emitter.color(quad.color, quad.color, quad.color, quad.color);
            emitter.emit();
        }
    }

    private static class Unbaked implements BlockStateModel.Unbaked {
        private final QuarryBlock quarryBlock;

        private Unbaked(QuarryBlock quarryBlock) {
            this.quarryBlock = quarryBlock;
        }
        public static Unbaked of(QuarryBlock quarryBlock) {
            return new Unbaked(quarryBlock);
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {

        }
        @Override
        public BlockStateModel bake(ModelBaker baker) {
            return new FabricQuarryModel(this.quarryBlock, baker);
        }
    }
}
