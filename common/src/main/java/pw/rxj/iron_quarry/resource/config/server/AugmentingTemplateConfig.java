package pw.rxj.iron_quarry.resource.config.server;

import pw.rxj.iron_quarry.resource.config.AbstractInnerConfigHandler;
import pw.rxj.iron_quarry.resource.config.ConfigHandler;
import pw.rxj.iron_quarry.resource.config.util.Comment;
import pw.rxj.iron_quarry.resource.config.util.Range;

public class AugmentingTemplateConfig {
    @Comment("Augment points a template can apply before it breaks.")
    @Range(min = 1)
    public final int durability;
    @Comment("Durability a capacity upgrade uses.")
    @Range(min = 0)
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
            return this.getServer().augmentingTemplate.durability;
        }
        public int getCapacityUpgradeCost() {
            return this.getServer().augmentingTemplate.capacityUpgradeCost;
        }
    }
}
