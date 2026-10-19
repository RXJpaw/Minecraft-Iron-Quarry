package pw.rxj.iron_quarry.mixin;

import net.fabricmc.api.EnvType;
import net.minecraft.recipe.PreparedRecipes;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.ServerRecipeManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.types.AugmentType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mixin(ServerRecipeManager.class)
public abstract class RecipeManagerMixin {
    @ModifyVariable(method = "apply(Lnet/minecraft/recipe/PreparedRecipes;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)V", at = @At(value = "HEAD"), argsOnly = true)
    private PreparedRecipes apply(PreparedRecipes preparedRecipes) {
        Main.CONFIG.read(EnvType.SERVER);
        Main.CONFIG.applyServerChanges();

        Set<Identifier> disabled = new HashSet<>();

        if(AugmentType.SPEED.isDisabled()) {
            disabled.add(Identifier.of(Main.MOD_ID, "augment_speed_tier1"));
            disabled.add(Identifier.of(Main.MOD_ID, "augment_speed_tier2"));
            disabled.add(Identifier.of(Main.MOD_ID, "augment_speed_tier3"));
        }
        if(AugmentType.FORTUNE.isDisabled()) {
            disabled.add(Identifier.of(Main.MOD_ID, "augment_fortune_tier1"));
            disabled.add(Identifier.of(Main.MOD_ID, "augment_fortune_tier2"));
            disabled.add(Identifier.of(Main.MOD_ID, "augment_fortune_tier3"));
        }

        if(disabled.isEmpty()) return preparedRecipes;

        List<RecipeEntry<?>> recipes = preparedRecipes.recipes().stream()
                .filter(recipe -> !disabled.contains(recipe.id().getValue()))
                .toList();

        return PreparedRecipes.of(recipes);
    }
}
