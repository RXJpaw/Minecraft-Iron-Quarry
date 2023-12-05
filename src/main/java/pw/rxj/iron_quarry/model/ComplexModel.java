package pw.rxj.iron_quarry.model;

import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.util.Identifier;

public interface ComplexModel extends UnbakedModel, BakedModel, FabricBakedModel {
    Identifier getModelSource();

    default Identifier getBlockPath() {
        Identifier modelSource = this.getModelSource();

        return Identifier.of(modelSource.getNamespace(), "block/" + modelSource.getPath());
    }
    default Identifier getItemPath() {
        Identifier modelSource = this.getModelSource();

        return Identifier.of(modelSource.getNamespace(), "item/" + modelSource.getPath());
    }
    default boolean isOf(Identifier resourceId) {
        return resourceId.equals(this.getBlockPath()) || resourceId.equals(this.getItemPath());
    }
}
