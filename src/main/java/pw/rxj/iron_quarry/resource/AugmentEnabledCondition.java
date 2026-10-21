package pw.rxj.iron_quarry.resource;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.types.AugmentType;

public class AugmentEnabledCondition implements ResourceCondition {
    public static final MapCodec<AugmentEnabledCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            AugmentType.CODEC.fieldOf("augment").forGetter(AugmentEnabledCondition::getAugmentType)
    ).apply(instance, AugmentEnabledCondition::of));
    public static final ResourceConditionType<AugmentEnabledCondition> TYPE = ResourceConditionType.create(Identifier.fromNamespaceAndPath(Main.MOD_ID, "augment_enabled"), CODEC);

    private final AugmentType augmentType;

    private AugmentEnabledCondition(AugmentType augmentType) {
        this.augmentType = augmentType;
    }
    public static AugmentEnabledCondition of(AugmentType augmentType) {
        return new AugmentEnabledCondition(augmentType);
    }

    protected AugmentType getAugmentType() {
        return this.augmentType;
    }

    @Override
    public ResourceConditionType<?> getType() {
        return TYPE;
    }

    @Override
    public boolean test(RegistryOps.RegistryInfoLookup registryInfo) {
        return !this.augmentType.isDisabled();
    }
}
