package pw.rxj.iron_quarry.model;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.SingleVariant;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.block.QuarryBlock;
import pw.rxj.iron_quarry.blockentity.QuarryBlockEntity;
import pw.rxj.iron_quarry.util.MachineConfiguration;

import java.util.function.Predicate;

@Environment(EnvType.CLIENT)
public class FabricQuarryModel extends QuarryModel {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(Main.MOD_ID, "quarry");

    private FabricQuarryModel(QuarryBlock quarryBlock, ModelBaker baker) {
        super(quarryBlock, baker);
    }

    public static void register() {
        CustomUnbakedBlockStateModel.register(ID, Unbaked.CODEC);
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

    private static class Unbaked implements CustomUnbakedBlockStateModel {
        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("block").forGetter(Unbaked::getBlock)
        ).apply(instance, Unbaked::of));

        private final Identifier block;

        private Unbaked(Identifier block) {
            this.block = block;
        }
        public static Unbaked of(Identifier block) {
            return new Unbaked(block);
        }

        protected Identifier getBlock() {
            return this.block;
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {

        }

        @Override
        public BlockStateModel bake(ModelBaker baker) {
            if(!(BuiltInRegistries.BLOCK.getValue(this.block) instanceof QuarryBlock quarryBlock)) return new SingleVariant(baker.missingBlockModelPart());

            return new FabricQuarryModel(quarryBlock, baker);
        }

        @Override
        public MapCodec<Unbaked> codec() {
            return CODEC;
        }
    }
}
