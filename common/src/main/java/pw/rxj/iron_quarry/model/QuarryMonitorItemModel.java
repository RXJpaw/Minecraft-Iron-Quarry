package pw.rxj.iron_quarry.model;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4fc;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.item.QuarryMonitorItem;
import pw.rxj.iron_quarry.platform.Platform;
import pw.rxj.iron_quarry.util.ZUtil;

@Environment(EnvType.CLIENT)
public class QuarryMonitorItemModel implements ItemModel {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(Main.MOD_ID, "quarry_monitor");

    private final ItemModel fallback;

    private QuarryMonitorItemModel(ItemModel fallback) {
        this.fallback = fallback;
    }
    public static QuarryMonitorItemModel of(ItemModel fallback) {
        return new QuarryMonitorItemModel(fallback);
    }

    @Override
    public void update(ItemStackRenderState state, ItemStack stack, ItemModelResolver resolver, ItemDisplayContext displayContext, @Nullable ClientLevel world, @Nullable ItemOwner heldItemContext, int seed) {
        state.appendModelIdentityElement(this);

        ItemStack quarryStack = this.getShownQuarryStack(stack, displayContext);
        if(quarryStack.isEmpty()) {
            this.fallback.update(state, stack, resolver, displayContext, world, heldItemContext, seed);
        } else {
            resolver.appendItemLayers(state, quarryStack, displayContext, world, heldItemContext, seed);
        }
    }

    private ItemStack getShownQuarryStack(ItemStack stack, ItemDisplayContext displayContext) {
        if(displayContext != ItemDisplayContext.GUI) return ItemStack.EMPTY;

        Minecraft client = Minecraft.getInstance();
        if(client.screen == null || !client.hasShiftDown()) return ItemStack.EMPTY;

        if(!(ZUtil.getBlockOrItem(stack) instanceof QuarryMonitorItem quarryMonitorItem)) return ItemStack.EMPTY;
        if(quarryMonitorItem.getCachedTimestamp(stack) <= 0) return ItemStack.EMPTY;

        return quarryMonitorItem.getCachedQuarryStack(stack);
    }

    public static void register() {
        Platform.CLIENT.registerItemModel(ID, Unbaked.CODEC);
    }

    public static class Unbaked implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ItemModels.CODEC.fieldOf("fallback").forGetter(Unbaked::getFallback)
        ).apply(instance, Unbaked::of));

        private final ItemModel.Unbaked fallback;

        private Unbaked(ItemModel.Unbaked fallback) {
            this.fallback = fallback;
        }
        public static Unbaked of(ItemModel.Unbaked fallback) {
            return new Unbaked(fallback);
        }

        protected ItemModel.Unbaked getFallback() {
            return this.fallback;
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            this.fallback.resolveDependencies(resolver);
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
            return QuarryMonitorItemModel.of(this.fallback.bake(context, transformation));
        }

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }
    }
}
