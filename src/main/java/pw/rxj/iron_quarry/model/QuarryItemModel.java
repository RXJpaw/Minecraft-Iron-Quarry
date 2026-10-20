package pw.rxj.iron_quarry.model;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.item.model.BasicItemModel;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.ModelSettings;
import net.minecraft.client.render.model.ResolvableModel;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.HeldItemContext;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.block.QuarryBlock;
import pw.rxj.iron_quarry.util.ZUtil;

import java.util.List;

@Environment(EnvType.CLIENT)
public record QuarryItemModel(QuarryModel model, ModelSettings settings) implements ItemModel {
    public static final Identifier ID = Identifier.of(Main.MOD_ID, "quarry");
    private static final Identifier BLOCK_PARENT = Identifier.ofVanilla("block/block");

    @Override
    public void update(ItemRenderState state, ItemStack stack, ItemModelManager resolver, ItemDisplayContext displayContext, @Nullable ClientWorld world, @Nullable HeldItemContext heldItemContext, int seed) {
        state.addModelKey(this);
        if(ZUtil.getBlockOrItem(stack) instanceof QuarryBlock quarryBlock) {
            state.addModelKey(quarryBlock.getMachineConfiguration(stack).write());
        }

        ItemRenderState.LayerRenderState layer = state.newLayer();

        layer.setRenderLayer(RenderLayers.getItemLayer(stack));
        this.settings.addSettings(layer, displayContext);
        List<BakedQuad> quads = this.model.getItemQuads(stack);
        layer.getQuads().addAll(quads);
        layer.setVertices(() -> BasicItemModel.bakeQuads(quads));
    }

    public record Unbaked(Identifier block) implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("block").forGetter(Unbaked::block)
        ).apply(instance, Unbaked::new));

        @Override
        public void resolve(ResolvableModel.Resolver resolver) {
            resolver.markDependency(BLOCK_PARENT);
        }

        @Override
        public ItemModel bake(ItemModel.BakeContext context) {
            if(!(Registries.BLOCK.get(this.block) instanceof QuarryBlock quarryBlock)) return context.missingItemModel();

            Baker baker = context.blockModelBaker();
            QuarryModel model = QuarryModel.bake(quarryBlock, baker);
            ModelSettings settings = new ModelSettings(true, model.particleSprite(), baker.getModel(BLOCK_PARENT).getTransformations());

            return new QuarryItemModel(model, settings);
        }

        @Override
        public MapCodec<Unbaked> getCodec() {
            return CODEC;
        }
    }
}
