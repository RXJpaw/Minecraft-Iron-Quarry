package pw.rxj.iron_quarry.resource.config.server;

import lombok.EqualsAndHashCode;
import pw.rxj.iron_quarry.item.ZItems;
import pw.rxj.iron_quarry.resource.config.AbstractInnerConfigHandler;
import pw.rxj.iron_quarry.resource.config.ConfigHandler;

@EqualsAndHashCode
public class AugmentingTemplateConfig {
    public final int durability;
    public final int capacityUpgradeCost;

    public AugmentingTemplateConfig() {
        this.durability = 18_300;
        this.capacityUpgradeCost = 1;
    }

    public static class Handler extends AbstractInnerConfigHandler {
        private Handler(ConfigHandler handler) {
            super(handler);
        }
        public static Handler of(ConfigHandler handler) {
            return new Handler(handler);
        }

        public int getDurability() {
            return this.server.augmentingTemplate.durability;
        }
        public int getCapacityUpgradeCost() {
            return this.server.augmentingTemplate.capacityUpgradeCost;
        }

        @Override
        public void applyChanges() {
            ZItems.AUGMENTING_TEMPLATE.getItem().override(this.getDurability());
            ZItems.AUGMENT.getItem().override(this.getCapacityUpgradeCost());
            ZItems.CHEST_LOOTING_AUGMENT.getItem().override(this.getCapacityUpgradeCost());
            ZItems.SILK_TOUCH_AUGMENT.getItem().override(this.getCapacityUpgradeCost());
        }
    }
}
