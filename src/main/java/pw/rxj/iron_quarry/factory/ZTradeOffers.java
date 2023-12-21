package pw.rxj.iron_quarry.factory;

import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.VillagerProfession;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.item.ZItems;
import pw.rxj.iron_quarry.resource.ConfigHandler;

import java.util.Optional;

public class ZTradeOffers {
    private static final ConfigHandler.SilkTouchAugmentConfigHandler SilkTouchAugmentConfig = Main.CONFIG.getSilkTouchAugmentConfig();

    public static TradeOffer SILK_TOUCH_AUGMENT(Entity entity, Random random) {
        ItemStack base = new ItemStack(Items.EMERALD_BLOCK, random.nextBetween(7, 21));
        ItemStack addition = new ItemStack(ZItems.CONDUCTIVE_AMETHYST.getItem(), 4);
        ItemStack result = new ItemStack(ZItems.SILK_TOUCH_AUGMENT.getItem(), 1);

        return new TradeOffer(base, addition, result, 3, 50, 0.25F);
    }

    //TODO: configurable villager profession for modpacks
    public static void register() {
        Optional<VillagerProfession> villagerProfession = SilkTouchAugmentConfig.getVillagerProfession();
        if(villagerProfession.isEmpty()) throw new Error("Invalid villager profession");

        TradeOfferHelper.registerVillagerOffers(villagerProfession.get(), SilkTouchAugmentConfig.getVillagerLevel(), factories -> {
            factories.add(ZTradeOffers::SILK_TOUCH_AUGMENT);
        });

        if(SilkTouchAugmentConfig.isWanderingVillagerEnabled()) {
            TradeOfferHelper.registerWanderingTraderOffers(SilkTouchAugmentConfig.getWanderingVillagerLevel(), factories -> {
                factories.add(ZTradeOffers::SILK_TOUCH_AUGMENT);
            });
        }
    }
}
