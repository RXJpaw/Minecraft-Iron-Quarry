package pw.rxj.iron_quarry.platform;

import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.model.quad.BakedColors;
import net.neoforged.neoforge.client.model.quad.BakedNormals;
import org.joml.Vector3fc;

public class NeoForgePlatformModel implements IPlatformModel {
    @Override
    public BakedQuad createBakedQuad(Vector3fc[] positions, long[] packedUVs, Direction direction, BakedQuad.MaterialInfo materialInfo) {
        return new BakedQuad(
                positions[0], positions[1], positions[2], positions[3],
                packedUVs[0], packedUVs[1], packedUVs[2], packedUVs[3],
                direction, materialInfo,
                BakedNormals.of(BakedNormals.computeQuadNormal(positions[0], positions[1], positions[2], positions[3])), BakedColors.DEFAULT
        );
    }
}
