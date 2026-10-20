package pw.rxj.iron_quarry.factory;

import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.item.ZItems;
import pw.rxj.iron_quarry.resource.config.server.SilkTouchAugmentConfig;

import java.util.Optional;

public class ZTradeOffers {
    private static final SilkTouchAugmentConfig.Handler SILK_TOUCH_AUGMENT_CONFIG = Main.CONFIG.getSilkTouchAugmentConfig();

    public static MerchantOffer SILK_TOUCH_AUGMENT(ServerLevel world, Entity entity, RandomSource random) {
        ItemStack baseStack = SILK_TOUCH_AUGMENT_CONFIG.getRandomizedBase(random);
        ItemCost base = new ItemCost(baseStack.getItem(), baseStack.getCount());

        ItemStack additionStack = SILK_TOUCH_AUGMENT_CONFIG.getRandomizedAddition(random);
        ItemCost addition = new ItemCost(additionStack.getItem(), additionStack.getCount());

        ItemStack result = new ItemStack(ZItems.SILK_TOUCH_AUGMENT.getItem(), 1);

        return new MerchantOffer(base, Optional.of(addition), result, 3, 50, 0.25F);
    }

    public static void register() {
        Main.LOGGER.info("Registering trade offers..");

        if(SILK_TOUCH_AUGMENT_CONFIG.isVillagerEnabled()) {
            ResourceKey<VillagerProfession> villagerProfession = BuiltInRegistries.VILLAGER_PROFESSION.getResourceKey(SILK_TOUCH_AUGMENT_CONFIG.getVillagerProfession()).orElseThrow();

            TradeOfferHelper.registerVillagerOffers(villagerProfession, SILK_TOUCH_AUGMENT_CONFIG.getVillagerLevel(), factories -> {
                factories.add(ZTradeOffers::SILK_TOUCH_AUGMENT);
            });
        }

        if(SILK_TOUCH_AUGMENT_CONFIG.isWanderingVillagerEnabled()) {
            Identifier pool = SILK_TOUCH_AUGMENT_CONFIG.getWanderingVillagerLevel() == 2
                    ? TradeOfferHelper.WanderingTraderOffersBuilder.SELL_SPECIAL_ITEMS_POOL
                    : TradeOfferHelper.WanderingTraderOffersBuilder.SELL_COMMON_ITEMS_POOL;

            TradeOfferHelper.registerWanderingTraderOffers(builder -> {
                builder.addOffersToPool(pool, ZTradeOffers::SILK_TOUCH_AUGMENT);
            });
        }
    }
}
