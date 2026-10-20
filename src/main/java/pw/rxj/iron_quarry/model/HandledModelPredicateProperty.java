package pw.rxj.iron_quarry.model;

import com.mojang.serialization.MapCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.item.property.numeric.NumericProperty;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HeldItemContext;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.interfaces.IModelPredicateProvider;

@Environment(EnvType.CLIENT)
public record HandledModelPredicateProperty() implements NumericProperty {
    public static final Identifier ID = Identifier.of(Main.MOD_ID, "handled_model_predicate");
    public static final MapCodec<HandledModelPredicateProperty> CODEC = MapCodec.unit(new HandledModelPredicateProperty());

    @Override
    public float getValue(ItemStack stack, @Nullable ClientWorld world, @Nullable HeldItemContext context, int seed) {
        if(stack.getItem() instanceof IModelPredicateProvider modelPredicateProvider) {
            return modelPredicateProvider.getModelPredicate(stack, world, context == null ? null : context.getEntity(), seed);
        }

        return 0.0F;
    }

    @Override
    public MapCodec<HandledModelPredicateProperty> getCodec() {
        return CODEC;
    }
}
