package pw.rxj.iron_quarry.interfaces;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public interface IModelPredicateProvider {
    float getModelPredicate(ItemStack stack, ClientWorld world, LivingEntity entity, int seed);
}
