package pw.rxj.iron_quarry.model;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ResolvableModel;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.Main;

@Environment(EnvType.CLIENT)
public record QuarryItemModel(BakedModel model) implements ItemModel {
    public static final Identifier ID = Identifier.of(Main.MOD_ID, "quarry");

    @Override
    public void update(ItemRenderState state, ItemStack stack, ItemModelManager resolver, ModelTransformationMode transformationMode, @Nullable ClientWorld world, @Nullable LivingEntity user, int seed) {
        ItemRenderState.LayerRenderState layer = state.newLayer();
        BakedModel bakedModel = this.model instanceof QuarryModel quarryModel ? quarryModel.forItem(stack) : this.model;

        layer.setModel(bakedModel, RenderLayers.getItemLayer(stack));
    }

    public record Unbaked(Identifier model) implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("model").forGetter(Unbaked::model)
        ).apply(instance, Unbaked::new));

        @Override
        public void resolve(ResolvableModel.Resolver resolver) {
            resolver.resolve(this.model);
        }

        @Override
        public ItemModel bake(ItemModel.BakeContext context) {
            return new QuarryItemModel(context.bake(this.model));
        }

        @Override
        public MapCodec<Unbaked> getCodec() {
            return CODEC;
        }
    }
}
