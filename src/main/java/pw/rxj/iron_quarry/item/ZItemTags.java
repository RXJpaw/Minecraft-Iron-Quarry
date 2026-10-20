package pw.rxj.iron_quarry.item;

import pw.rxj.iron_quarry.Main;

import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ZItemTags {
    private static TagKey<Item> of(String path) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Main.MOD_ID, path));
    }
    private static TagKey<Item> commonOf(String path) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", path));
    }

    public static final TagKey<Item> AUGMENT_CAPACITY_ENHANCERS = of("augment_capacity_enhancers");
    public static final TagKey<Item> AUGMENT_FORTUNE_T1_ENHANCERS = of("augment_fortune_tier1_enhancers");
    public static final TagKey<Item> AUGMENT_FORTUNE_T2_ENHANCERS = of("augment_fortune_tier2_enhancers");
    public static final TagKey<Item> AUGMENT_FORTUNE_T3_ENHANCERS = of("augment_fortune_tier3_enhancers");
    public static final TagKey<Item> AUGMENT_SPEED_T1_ENHANCERS = of("augment_speed_tier1_enhancers");
    public static final TagKey<Item> AUGMENT_SPEED_T2_ENHANCERS = of("augment_speed_tier2_enhancers");
    public static final TagKey<Item> AUGMENT_SPEED_T3_ENHANCERS = of("augment_speed_tier3_enhancers");
    public static final TagKey<Item> C_WRENCHES = commonOf("tools/wrench");

    public static List<TagKey<Item>> AUGMENT_ENHANCERS = List.of(
            ZItemTags.AUGMENT_FORTUNE_T1_ENHANCERS,
            ZItemTags.AUGMENT_FORTUNE_T2_ENHANCERS,
            ZItemTags.AUGMENT_FORTUNE_T3_ENHANCERS,

            ZItemTags.AUGMENT_SPEED_T1_ENHANCERS,
            ZItemTags.AUGMENT_SPEED_T2_ENHANCERS,
            ZItemTags.AUGMENT_SPEED_T3_ENHANCERS
    );
}
