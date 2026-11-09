package pw.rxj.iron_quarry.item;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.interfaces.IModifyCreativeTabEntries;
import pw.rxj.iron_quarry.types.ExpansionType;
import pw.rxj.iron_quarry.types.MiningPattern;
import pw.rxj.iron_quarry.util.ReadableString;

import java.util.function.Consumer;

public class ExpansionCardItem extends Item implements IModifyCreativeTabEntries {
    private final ExpansionType type;
    private final @Nullable MiningPattern miningPattern;

    private ExpansionCardItem(Item.Properties settings, ExpansionType type, @Nullable MiningPattern miningPattern) {
        super(settings);

        this.type = type;
        this.miningPattern = miningPattern;
    }
    public static ExpansionCardItem of(Item.Properties settings, ExpansionType type) {
        if(type == ExpansionType.MINING_PATTERN) throw new IllegalArgumentException("Mining pattern expansion cards have to be created with ExpansionCardItem#ofMiningPattern.");

        return new ExpansionCardItem(settings, type, null);
    }
    public static ExpansionCardItem ofMiningPattern(Item.Properties settings, MiningPattern miningPattern) {
        return new ExpansionCardItem(settings, ExpansionType.MINING_PATTERN, miningPattern);
    }

    public ExpansionType getType() {
        return this.type;
    }
    public boolean isMiningPattern() {
        return this.type == ExpansionType.MINING_PATTERN;
    }
    public MiningPattern getMiningPattern() {
        if(this.miningPattern == null) throw new IllegalStateException("ExpansionCardItem#getMiningPattern can only be called on mining pattern expansion cards.");

        return this.miningPattern;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> textConsumer, TooltipFlag options) {
        if(this.isMiningPattern()) {
            textConsumer.accept(ReadableString.translatable("item.iron_quarry.expansion.lore.benefit.mining_pattern." + this.getMiningPattern().getName()));
            textConsumer.accept(ReadableString.translatable("item.iron_quarry.expansion.lore.mining_pattern"));
        } else {
            textConsumer.accept(ReadableString.translatable("item.iron_quarry.expansion.lore.benefit." + this.type.getName()));
        }

        if(this.type.getExpansion().isDisabled()) {
            MutableComponent LORE_DISABLED = ReadableString.translatable("item.iron_quarry.lore.disabled");

            textConsumer.accept(ReadableString.space());
            textConsumer.accept(LORE_DISABLED);
        }
    }

    @Override
    public void modifyCreativeTabEntries(CreativeModeTab.Output entries) {
        if(this.type.getExpansion().isDisabled()) return;

        entries.accept(this);
    }
}
