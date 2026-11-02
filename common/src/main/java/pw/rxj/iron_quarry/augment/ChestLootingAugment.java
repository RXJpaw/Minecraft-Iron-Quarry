package pw.rxj.iron_quarry.augment;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.compat.Compat;
import pw.rxj.iron_quarry.item.ZItems;
import pw.rxj.iron_quarry.resource.config.server.AugmentsConfig;
import pw.rxj.iron_quarry.util.ReadableString;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;

public class ChestLootingAugment extends AbstractAugment {
    private final AugmentsConfig.Handler config;

    private ChestLootingAugment(AugmentsConfig.Handler config) {
        this.config = config;
    }
    public static ChestLootingAugment of(AugmentsConfig.Handler config) {
        return new ChestLootingAugment(config);
    }

    @Override
    public boolean isDisabled() {
        return this.config.getChestLooting().disabled;
    }
    @Override
    public int getBaseAmount() {
        return 0;
    }

    public @Nullable LootPool.Builder getLootPoolBuilder(Identifier id) {
        Optional<AugmentsConfig.ChestLooting.Entry> optionalEntry = Arrays.stream(this.config.getChestLooting().lootPools).filter(el -> el.lootTable.equals(id.toString())).findAny();
        if(optionalEntry.isEmpty()) return null;
        AugmentsConfig.ChestLooting.Entry entry = optionalEntry.get();

        return LootPool.lootPool()
                .setRolls(UniformGenerator.between(entry.rollsMin, entry.rollsMax))
                .when(LootItemRandomChanceCondition.randomChance(entry.chance))
                .add(LootItem.lootTableItem(ZItems.CHEST_LOOTING_AUGMENT));
    }

    @Override
    public void appendEffectTooltip(int amount, Consumer<Component> textConsumer) {
        MutableComponent LORE_BENEFIT = ReadableString.translatable("item.iron_quarry.augment.lore.benefit.chest_looting");
        MutableComponent LORE_DRAWBACK = ReadableString.translatable("item.iron_quarry.augment.lore.drawback.time");

        textConsumer.accept(LORE_BENEFIT);
        textConsumer.accept(LORE_DRAWBACK);

        if(Compat.LOOTR_LOADED) {
            ReadableString.translatableLines("item.iron_quarry.augment.lore.lootr_exploration").forEach(textConsumer);

            if(Minecraft.getInstance().hasShiftDown()) {
                ReadableString.translatableLines("item.iron_quarry.augment.lore.lootr_conversion").forEach(textConsumer);
            } else {
                MutableComponent LORE_DETAILS = ReadableString.translatable("item.iron_quarry.lore.details");

                textConsumer.accept(ReadableString.space());
                textConsumer.accept(LORE_DETAILS);
            }
        }
    }
}
