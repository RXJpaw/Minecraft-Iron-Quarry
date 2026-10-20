package pw.rxj.iron_quarry.model;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.item.BlockModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.block.QuarryBlock;
import pw.rxj.iron_quarry.util.ZUtil;

import java.util.List;

@Environment(EnvType.CLIENT)
public record QuarryItemModel(QuarryModel model, ModelRenderProperties settings) implements ItemModel {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(Main.MOD_ID, "quarry");
    private static final Identifier BLOCK_PARENT = Identifier.withDefaultNamespace("block/block");

    @Override
    public void update(ItemStackRenderState state, ItemStack stack, ItemModelResolver resolver, ItemDisplayContext displayContext, @Nullable ClientLevel world, @Nullable ItemOwner heldItemContext, int seed) {
        state.appendModelIdentityElement(this);
        if(ZUtil.getBlockOrItem(stack) instanceof QuarryBlock quarryBlock) {
            state.appendModelIdentityElement(quarryBlock.getMachineConfiguration(stack).write());
        }

        ItemStackRenderState.LayerRenderState layer = state.newLayer();

        layer.setRenderType(Sheets.cutoutBlockSheet());
        this.settings.applyToLayer(layer, displayContext);
        List<BakedQuad> quads = this.model.getItemQuads(stack);
        layer.prepareQuadList().addAll(quads);
        layer.setExtents(() -> BlockModelWrapper.computeExtents(quads));
    }

    public record Unbaked(Identifier block) implements ItemModel.Unbaked {
        public static final MapCodec<pw.rxj.iron_quarry.model.QuarryItemModel.Unbaked> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("block").forGetter(pw.rxj.iron_quarry.model.QuarryItemModel.Unbaked::block)
        ).apply(instance, pw.rxj.iron_quarry.model.QuarryItemModel.Unbaked::new));

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            resolver.markDependency(BLOCK_PARENT);
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context) {
            if(!(BuiltInRegistries.BLOCK.getValue(this.block) instanceof QuarryBlock quarryBlock)) return context.missingItemModel();

            ModelBaker baker = context.blockModelBaker();
            QuarryModel model = QuarryModel.bake(quarryBlock, baker);
            ModelRenderProperties settings = new ModelRenderProperties(true, model.particleIcon(), baker.getModel(BLOCK_PARENT).getTopTransforms());

            return new QuarryItemModel(model, settings);
        }

        @Override
        public MapCodec<pw.rxj.iron_quarry.model.QuarryItemModel.Unbaked> type() {
            return CODEC;
        }
    }
}
