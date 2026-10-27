package pw.rxj.iron_quarry.model;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import pw.rxj.iron_quarry.block.QuarryBlock;
import pw.rxj.iron_quarry.block.ZBlocks;

import java.util.List;

public class ZModels {
    private static final List<QuarryBlock> quarryBlockList = List.of(
            ZBlocks.COPPER_QUARRY.getBlock(),
            ZBlocks.IRON_QUARRY.getBlock(),
            ZBlocks.GOLD_QUARRY.getBlock(),
            ZBlocks.DIAMOND_QUARRY.getBlock(),
            ZBlocks.NETHERITE_QUARRY.getBlock(),
            ZBlocks.NETHER_STAR_QUARRY.getBlock()
    );

    public static void register() {
        ModelLoadingPlugin.register(pluginContext -> {
            for (QuarryBlock quarryBlock : quarryBlockList) {
                FabricQuarryModel.register(pluginContext, quarryBlock);
            }
        });
    }
}
