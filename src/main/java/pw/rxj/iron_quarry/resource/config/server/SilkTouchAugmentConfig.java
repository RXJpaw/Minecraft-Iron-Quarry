package pw.rxj.iron_quarry.resource.config.server;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import pw.rxj.iron_quarry.resource.config.AbstractInnerConfigHandler;
import pw.rxj.iron_quarry.resource.config.ConfigHandler;

import java.util.Optional;

@EqualsAndHashCode
public class SilkTouchAugmentConfig {
    public final String villagerProfession = "minecraft:toolsmith";
    public final byte villagerLevel = 3;
    public final boolean wanderingVillager = true;
    public final byte wanderingVillagerLevel = 2;
    public final Price price = new Price("minecraft:emerald", (byte) 40, (byte) 64, "iron_quarry:circuit_mk3", (byte) 3, (byte) 7);

    @EqualsAndHashCode
    @AllArgsConstructor
    public static class Price {
        public final String base;
        public final byte baseLow;
        public final byte baseHigh;
        public final String addition;
        public final byte additionLow;
        public final byte additionHigh;
    }

    public static class Handler extends AbstractInnerConfigHandler {
        private Handler(ConfigHandler handler) {
            super(handler);
        }
        public static Handler of(ConfigHandler handler) {
            return new Handler(handler);
        }

        @SuppressWarnings("ConstantConditions")
        public boolean isVillagerEnabled() {
            return !this.server.silkTouchAugment.villagerProfession.equals("minecraft:none");
        }
        public VillagerProfession getVillagerProfession() {
            Optional<VillagerProfession> villagerProfession = BuiltInRegistries.VILLAGER_PROFESSION.getOptional(Identifier.tryParse(this.server.silkTouchAugment.villagerProfession));
            if(villagerProfession.isEmpty()) throw new Error("Invalid villager profession: " + this.server.silkTouchAugment.villagerProfession);

            return villagerProfession.get();
        }
        public byte getVillagerLevel() {
            return this.server.silkTouchAugment.villagerLevel;
        }

        public boolean isWanderingVillagerEnabled() {
            return this.server.silkTouchAugment.wanderingVillager;
        }
        public byte getWanderingVillagerLevel() {
            return this.server.silkTouchAugment.wanderingVillagerLevel;
        }

        public ItemStack getRandomizedBase(RandomSource random) {
            Optional<Item> baseItem = BuiltInRegistries.ITEM.getOptional(Identifier.tryParse(this.server.silkTouchAugment.price.base));
            if(baseItem.isEmpty()) throw new Error("Invalid price base: " + this.server.silkTouchAugment.price.base);

            return new ItemStack(baseItem.get(), random.nextIntBetweenInclusive(this.server.silkTouchAugment.price.baseLow, this.server.silkTouchAugment.price.baseHigh));
        }
        public ItemStack getRandomizedAddition(RandomSource random) {
            Optional<Item> additionItem = BuiltInRegistries.ITEM.getOptional(Identifier.tryParse(this.server.silkTouchAugment.price.addition));
            if(additionItem.isEmpty()) throw new Error("Invalid price addition: " + this.server.silkTouchAugment.price.addition);

            return new ItemStack(additionItem.get(), random.nextIntBetweenInclusive(this.server.silkTouchAugment.price.additionLow, this.server.silkTouchAugment.price.additionHigh));
        }
    }
}
