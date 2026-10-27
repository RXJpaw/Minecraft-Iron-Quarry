package pw.rxj.iron_quarry.platform;

import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import org.joml.Vector3fc;

public interface IPlatformModel {
    BakedQuad createBakedQuad(Vector3fc[] positions, long[] packedUVs, Direction direction, BakedQuad.MaterialInfo materialInfo);
}
