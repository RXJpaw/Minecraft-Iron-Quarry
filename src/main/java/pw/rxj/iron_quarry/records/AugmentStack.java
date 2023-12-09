package pw.rxj.iron_quarry.records;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.tag.TagKey;
import pw.rxj.iron_quarry.item.ZItemTags;
import pw.rxj.iron_quarry.types.AugmentType;

public record AugmentStack(AugmentType type, int amount) {
    public static AugmentStack from(ItemStack stack) {
        int amount = stack.getCount();

        if(Ingredient.fromTag(ZItemTags.AUGMENT_FORTUNE_T1_ENHANCERS).test(stack)) {
            return new AugmentStack(AugmentType.FORTUNE, amount);
        } else if(Ingredient.fromTag(ZItemTags.AUGMENT_FORTUNE_T2_ENHANCERS).test(stack)){
            return new AugmentStack(AugmentType.FORTUNE, amount * 9);
        } else if(Ingredient.fromTag(ZItemTags.AUGMENT_FORTUNE_T3_ENHANCERS).test(stack)){
            return new AugmentStack(AugmentType.FORTUNE, amount * 81);
        }

        if(Ingredient.fromTag(ZItemTags.AUGMENT_SPEED_T1_ENHANCERS).test(stack)) {
            return new AugmentStack(AugmentType.SPEED, amount);
        } else if(Ingredient.fromTag(ZItemTags.AUGMENT_SPEED_T2_ENHANCERS).test(stack)){
            return new AugmentStack(AugmentType.SPEED, amount * 9);
        } else if(Ingredient.fromTag(ZItemTags.AUGMENT_SPEED_T3_ENHANCERS).test(stack)){
            return new AugmentStack(AugmentType.SPEED, amount * 81);
        }

        return null;
    }

    public static AugmentStack from(TagKey<Item> itemTagKey) {
        if(itemTagKey.equals(ZItemTags.AUGMENT_FORTUNE_T1_ENHANCERS)) {
            return new AugmentStack(AugmentType.FORTUNE, 1);
        } else if(itemTagKey.equals(ZItemTags.AUGMENT_FORTUNE_T2_ENHANCERS)) {
            return new AugmentStack(AugmentType.FORTUNE, 9);
        } else if(itemTagKey.equals(ZItemTags.AUGMENT_FORTUNE_T3_ENHANCERS)) {
            return new AugmentStack(AugmentType.FORTUNE, 81);
        }

        if(itemTagKey.equals(ZItemTags.AUGMENT_SPEED_T1_ENHANCERS)) {
            return new AugmentStack(AugmentType.SPEED, 1);
        } else if(itemTagKey.equals(ZItemTags.AUGMENT_SPEED_T2_ENHANCERS)) {
            return new AugmentStack(AugmentType.SPEED, 9);
        } else if(itemTagKey.equals(ZItemTags.AUGMENT_SPEED_T3_ENHANCERS)) {
            return new AugmentStack(AugmentType.SPEED, 81);
        }

        return null;
    }
}
