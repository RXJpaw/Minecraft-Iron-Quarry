package pw.rxj.iron_quarry.model;

import com.mojang.serialization.MapCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.interfaces.IModelPredicateProvider;

@Environment(EnvType.CLIENT)
public class HandledModelPredicateProperty implements RangeSelectItemModelProperty {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(Main.MOD_ID, "handled_model_predicate");
    public static final MapCodec<HandledModelPredicateProperty> CODEC = MapCodec.unit(new HandledModelPredicateProperty());

    private HandledModelPredicateProperty() { }

    @Override
    public float get(ItemStack stack, @Nullable ClientLevel world, @Nullable ItemOwner context, int seed) {
        if(stack.getItem() instanceof IModelPredicateProvider modelPredicateProvider) {
            return modelPredicateProvider.getModelPredicate(stack, world, context == null ? null : context.asLivingEntity(), seed);
        }

        return 0.0F;
    }

    @Override
    public MapCodec<HandledModelPredicateProperty> type() {
        return CODEC;
    }
}
