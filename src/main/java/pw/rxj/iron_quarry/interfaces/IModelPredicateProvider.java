package pw.rxj.iron_quarry.interfaces;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface IModelPredicateProvider {
    float getModelPredicate(ItemStack stack, ClientLevel world, LivingEntity entity, int seed);
}
