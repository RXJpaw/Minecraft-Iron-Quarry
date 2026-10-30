package pw.rxj.iron_quarry.augment;

import pw.rxj.iron_quarry.Main;

public class ZAugments {
    public static final EmptyAugment EMPTY = EmptyAugment.of();
    public static final SpeedAugment SPEED = SpeedAugment.of(Main.CONFIG.getAugmentsConfig());
    public static final FortuneAugment FORTUNE = FortuneAugment.of(Main.CONFIG.getAugmentsConfig());
    public static final SilkTouchAugment SILK_TOUCH = SilkTouchAugment.of(Main.CONFIG.getAugmentsConfig());
    public static final ChestLootingAugment CHEST_LOOTING = ChestLootingAugment.of(Main.CONFIG.getAugmentsConfig());
}
