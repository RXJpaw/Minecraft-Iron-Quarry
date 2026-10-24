package pw.rxj.iron_quarry.data;

import com.mojang.serialization.Codec;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.types.AugmentType;
import pw.rxj.iron_quarry.util.MachineConfiguration;
import pw.rxj.iron_quarry.util.QuarryInventory;

import java.util.List;
import java.util.function.UnaryOperator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.Level;

public class ZComponents {
    public static DataComponentType<Integer> CUSTOM_DAMAGE = register("custom_damage", (builder) -> builder
            .persistent(Codec.INT)
            .networkSynchronized(ByteBufCodecs.VAR_INT));

    public static DataComponentType<Identifier> SMITHING_OUTPUT_PREVIEW = register("smithing_output_preview", (builder) -> builder
            .persistent(Identifier.CODEC)
            .networkSynchronized(Identifier.STREAM_CODEC));

    public static DataComponentType<AugmentType> AUGMENT_TYPE = register("augment_type", (builder) -> builder
            .persistent(AugmentType.CODEC)
            .networkSynchronized(AugmentType.PACKET_CODEC));
    public static DataComponentType<Integer> AUGMENT_AMOUNT = register("augment_amount", (builder) -> builder
            .persistent(Codec.INT)
            .networkSynchronized(ByteBufCodecs.VAR_INT));
    public static DataComponentType<List<Identifier>> AUGMENT_UPGRADES = register("augment_upgrades", (builder) -> builder
            .persistent(Identifier.CODEC.listOf())
            .networkSynchronized(Identifier.STREAM_CODEC.apply(ByteBufCodecs.list()))
            .cacheEncoding());

    public static DataComponentType<Boolean> BLUEPRINT_SEALED = register("blueprint_sealed", (builder) -> builder
            .persistent(Codec.BOOL)
            .networkSynchronized(ByteBufCodecs.BOOL));
    public static DataComponentType<ResourceKey<Level>> BLUEPRINT_WORLD = register("blueprint_world", (builder) -> builder
            .persistent(Identifier.CODEC.xmap((id) -> ResourceKey.create(Registries.DIMENSION, id), ResourceKey::identifier))
            .networkSynchronized(Identifier.STREAM_CODEC.map((id) -> ResourceKey.create(Registries.DIMENSION, id), ResourceKey::identifier)));
    public static DataComponentType<BlueprintPositionComponent> BLUEPRINT_POSITION = register("blueprint_position", (builder) -> builder
            .persistent(BlueprintPositionComponent.CODEC)
            .networkSynchronized(BlueprintPositionComponent.PACKET_CODEC));
    public static DataComponentType<Long> BLUEPRINT_MINED_CHUNKS = register("blueprint_mined_chunks", (builder) -> builder
            .persistent(Codec.LONG)
            .networkSynchronized(ByteBufCodecs.VAR_LONG));

    public static DataComponentType<ResourceKey<Level>> QUARRY_MONITOR_WORLD = register("quarry_monitor_world", (builder) -> builder
            .persistent(Identifier.CODEC.xmap((id) -> ResourceKey.create(Registries.DIMENSION, id), ResourceKey::identifier))
            .networkSynchronized(Identifier.STREAM_CODEC.map((id) -> ResourceKey.create(Registries.DIMENSION, id), ResourceKey::identifier)));
    public static DataComponentType<BlockPos> QUARRY_MONITOR_POSITION = register("quarry_monitor_position", (builder) -> builder
            .persistent(BlockPos.CODEC)
            .networkSynchronized(BlockPos.STREAM_CODEC));
    public static DataComponentType<ItemStackTemplate> QUARRY_MONITOR_CACHED_QUARRY = register("quarry_monitor_cached_quarry", (builder) -> builder
            .persistent(ItemStackTemplate.CODEC)
            .networkSynchronized(ItemStackTemplate.STREAM_CODEC));
    public static DataComponentType<Long> QUARRY_MONITOR_CACHED_TIMESTAMP = register("quarry_monitor_cached_timestamp", (builder) -> builder
            .persistent(Codec.LONG)
            .networkSynchronized(ByteBufCodecs.VAR_LONG));


    /**
     * {@link net.minecraft.core.component.DataComponents}
     */
    public static <T> DataComponentType<T> register(String id, UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Identifier.fromNamespaceAndPath(Main.MOD_ID, id), (builderOperator.apply(DataComponentType.builder())).build());
    }

    public static void register() {
        var ignored = List.of(
                MachineConfiguration.COMPONENT,
                QuarryInventory.Output.COMPONENT,
                QuarryInventory.BatteryInput.COMPONENT,
                QuarryInventory.Blueprint.COMPONENT,
                QuarryInventory.Drill.COMPONENT,
                QuarryInventory.MachineUpgrades.COMPONENT
        );
    }}
