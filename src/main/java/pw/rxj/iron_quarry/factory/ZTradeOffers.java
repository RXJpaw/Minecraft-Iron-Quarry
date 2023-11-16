package pw.rxj.iron_quarry.factory;

import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.VillagerProfession;
import pw.rxj.iron_quarry.items.ZItems;

public class ZTradeOffers {
    public static TradeOffer SILK_TOUCH_AUGMENT(Entity entity, Random random) {
        ItemStack base = new ItemStack(Items.EMERALD_BLOCK, random.nextBetween(7, 21));
        ItemStack addition = new ItemStack(ZItems.CONDUCTIVE_AMETHYST.getItem(), 4);
        ItemStack result = new ItemStack(ZItems.SILK_TOUCH_AUGMENT.getItem(), 1);

        return new TradeOffer(base, addition, result, 3, 50, 0.25F);
    }

    public static void register() {
        TradeOfferHelper.registerVillagerOffers(VillagerProfession.TOOLSMITH, 4, factories -> {
            factories.add(ZTradeOffers::SILK_TOUCH_AUGMENT);
        });

        TradeOfferHelper.registerWanderingTraderOffers(4, factories -> {
            factories.add(ZTradeOffers::SILK_TOUCH_AUGMENT);
        });
    }
}
