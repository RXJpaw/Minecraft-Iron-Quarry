package pw.rxj.iron_quarry.model;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.SingleVariant;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.event.RegisterBlockStateModels;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import net.neoforged.neoforge.client.model.quad.BakedColors;
import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.block.QuarryBlock;
import pw.rxj.iron_quarry.blockentity.QuarryBlockEntity;
import pw.rxj.iron_quarry.util.MachineConfiguration;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class NeoForgeQuarryModel extends QuarryModel {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(Main.MOD_ID, "quarry");

    private NeoForgeQuarryModel(QuarryBlock quarryBlock, ModelBaker baker) {
        super(quarryBlock, baker);
    }

    public static void register(RegisterBlockStateModels event) {
        event.registerModel(ID, Unbaked.CODEC);
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        MachineConfiguration configuration = level.getBlockEntity(pos) instanceof QuarryBlockEntity blockEntity ? blockEntity.Configuration : MachineConfiguration.empty();

        Direction frontDirection = state.getOptionalValue(QuarryBlock.FACING).orElse(Direction.NORTH);

        parts.add(Part.of(this.getQuads(frontDirection, configuration, true), PARTICLE_SPRITE));
    }

    @Override
    public void collectParts(RandomSource random, List<BlockStateModelPart> parts) {
        parts.add(Part.of(this.getQuads(Direction.NORTH, MachineConfiguration.empty(), true), PARTICLE_SPRITE));
    }

    private static class Part implements BlockStateModelPart {
        private final Map<Direction, List<BakedQuad>> quadsByCullFace = new EnumMap<>(Direction.class);
        private final Material.Baked particleMaterial;

        private Part(List<Quad> quads, Material.Baked particleMaterial) {
            this.particleMaterial = particleMaterial;

            for (Quad quad : quads) {
                this.quadsByCullFace.computeIfAbsent(quad.cullFace(), direction -> new ArrayList<>()).add(toColoredBakedQuad(quad));
            }
        }
        public static Part of(List<Quad> quads, Material.Baked particleMaterial) {
            return new Part(quads, particleMaterial);
        }

        private static BakedQuad toColoredBakedQuad(Quad quad) {
            BakedQuad bakedQuad = quad.toBakedQuad();
            if(quad.color == -1) return bakedQuad;

            return new BakedQuad(
                    bakedQuad.position0(), bakedQuad.position1(), bakedQuad.position2(), bakedQuad.position3(),
                    bakedQuad.packedUV0(), bakedQuad.packedUV1(), bakedQuad.packedUV2(), bakedQuad.packedUV3(),
                    bakedQuad.direction(), bakedQuad.materialInfo(), bakedQuad.bakedNormals(), BakedColors.of(quad.color)
            );
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable Direction direction) {
            if(direction == null) return List.of();

            return this.quadsByCullFace.getOrDefault(direction, List.of());
        }

        @Override
        public boolean useAmbientOcclusion() {
            return true;
        }

        @Override
        public Material.Baked particleMaterial() {
            return this.particleMaterial;
        }

        @Override
        public int materialFlags() {
            return 0;
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

            return new NeoForgeQuarryModel(quarryBlock, baker);
        }

        @Override
        public MapCodec<Unbaked> codec() {
            return CODEC;
        }
    }
}
