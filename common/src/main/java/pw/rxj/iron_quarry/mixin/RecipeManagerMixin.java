package pw.rxj.iron_quarry.mixin;

import pw.rxj.iron_quarry.resource.config.ConfigSide;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.types.AugmentType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {
    @ModifyVariable(method = "apply(Lnet/minecraft/world/item/crafting/RecipeMap;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At(value = "HEAD"), argsOnly = true)
    private RecipeMap apply(RecipeMap preparedRecipes) {
        Main.CONFIG.read(ConfigSide.SERVER);
        Main.CONFIG.applyServerChanges();

        Set<Identifier> disabled = new HashSet<>();

        if(AugmentType.SPEED.isDisabled()) {
            disabled.add(Identifier.fromNamespaceAndPath(Main.MOD_ID, "augment_speed_tier1"));
            disabled.add(Identifier.fromNamespaceAndPath(Main.MOD_ID, "augment_speed_tier2"));
            disabled.add(Identifier.fromNamespaceAndPath(Main.MOD_ID, "augment_speed_tier3"));
        }
        if(AugmentType.FORTUNE.isDisabled()) {
            disabled.add(Identifier.fromNamespaceAndPath(Main.MOD_ID, "augment_fortune_tier1"));
            disabled.add(Identifier.fromNamespaceAndPath(Main.MOD_ID, "augment_fortune_tier2"));
            disabled.add(Identifier.fromNamespaceAndPath(Main.MOD_ID, "augment_fortune_tier3"));
        }

        if(disabled.isEmpty()) return preparedRecipes;

        List<RecipeHolder<?>> recipes = preparedRecipes.values().stream()
                .filter(recipe -> !disabled.contains(recipe.id().identifier()))
                .toList();

        return RecipeMap.create(recipes);
    }
}
