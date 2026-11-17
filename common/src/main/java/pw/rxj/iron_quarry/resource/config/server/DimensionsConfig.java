package pw.rxj.iron_quarry.resource.config.server;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.resource.config.AbstractInnerConfigHandler;
import pw.rxj.iron_quarry.resource.config.ConfigHandler;
import pw.rxj.iron_quarry.resource.config.util.Comment;
import pw.rxj.iron_quarry.resource.config.util.Options;
import pw.rxj.iron_quarry.types.DimensionListMode;

import java.util.Arrays;

public class DimensionsConfig {
    private static final String NAMESPACE_WILDCARD = ":*";

    @Comment("\"blacklist\": Quarries mine in every dimension except the listed ones.\n\"whitelist\": Quarries only mine in the listed dimensions.")
    @Options(DimensionListMode.class)
    public final String mode;
    @Comment("Dimension IDs like minecraft:the_end, or somemod:* for every dimension of a mod.")
    public final String[] list;

    public DimensionsConfig() {
        this.mode = DimensionListMode.BLACKLIST.getOptionName();
        this.list = new String[0];
    }

    public boolean isWhitelist() {
        return DimensionListMode.byOptionName(this.mode) == DimensionListMode.WHITELIST;
    }
    public boolean allows(Identifier dimension) {
        return Arrays.stream(this.list).anyMatch(entry -> matches(entry, dimension)) == this.isWhitelist();
    }

    private static boolean isNamespaceWildcard(String entry) {
        return entry.endsWith(NAMESPACE_WILDCARD);
    }
    private static String getWildcardNamespace(String entry) {
        return entry.substring(0, entry.length() - NAMESPACE_WILDCARD.length());
    }
    private static boolean matches(String entry, Identifier dimension) {
        if(isNamespaceWildcard(entry)) return dimension.getNamespace().equals(getWildcardNamespace(entry));

        return dimension.equals(Identifier.tryParse(entry));
    }
    private static boolean isValidEntry(String entry) {
        if(isNamespaceWildcard(entry)) return Identifier.isValidNamespace(getWildcardNamespace(entry));

        return Identifier.tryParse(entry) != null;
    }

    public static class Handler extends AbstractInnerConfigHandler {
        private Handler(ConfigHandler handler) {
            super(handler);
        }
        public static Handler of(ConfigHandler handler) {
            return new Handler(handler);
        }

        public DimensionsConfig getDimensions() {
            return this.getServer().dimensions;
        }
        public boolean canMineIn(ResourceKey<Level> worldKey) {
            return this.getDimensions().allows(worldKey.identifier());
        }

        public void validate() {
            for(String entry : this.getDimensions().list) {
                if(isValidEntry(entry)) continue;

                Main.LOGGER.error("Config dimensions.list entry {} is not a valid dimension ID!", entry);
            }
        }
    }
}
