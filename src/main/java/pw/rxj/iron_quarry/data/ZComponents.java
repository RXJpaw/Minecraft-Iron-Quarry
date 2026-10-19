package pw.rxj.iron_quarry.data;

import com.mojang.serialization.Codec;
import net.minecraft.component.DataComponentType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.types.AugmentType;
import pw.rxj.iron_quarry.util.MachineConfiguration;
import pw.rxj.iron_quarry.util.QuarryInventory;

import java.util.List;
import java.util.function.UnaryOperator;

public class ZComponents {
    public static DataComponentType<Integer> CUSTOM_DAMAGE = register("custom_damage", (builder) -> builder
            .codec(Codec.INT)
            .packetCodec(PacketCodecs.VAR_INT));

    public static DataComponentType<Identifier> SMITHING_OUTPUT_PREVIEW = register("smithing_output_preview", (builder) -> builder
            .codec(Identifier.CODEC)
            .packetCodec(Identifier.PACKET_CODEC));

    public static DataComponentType<AugmentType> AUGMENT_TYPE = register("augment_type", (builder) -> builder
            .codec(AugmentType.CODEC)
            .packetCodec(AugmentType.PACKET_CODEC));
    public static DataComponentType<Integer> AUGMENT_AMOUNT = register("augment_amount", (builder) -> builder
            .codec(Codec.INT)
            .packetCodec(PacketCodecs.VAR_INT));
    public static DataComponentType<List<Identifier>> AUGMENT_UPGRADES = register("augment_upgrades", (builder) -> builder
            .codec(Identifier.CODEC.listOf())
            .packetCodec(Identifier.PACKET_CODEC.collect(PacketCodecs.toList()))
            .cache());

    public static DataComponentType<Boolean> BLUEPRINT_SEALED = register("blueprint_sealed", (builder) -> builder
            .codec(Codec.BOOL)
            .packetCodec(PacketCodecs.BOOL));
    public static DataComponentType<RegistryKey<World>> BLUEPRINT_WORLD = register("blueprint_world", (builder) -> builder
            .codec(Identifier.CODEC.xmap((id) -> RegistryKey.of(RegistryKeys.WORLD, id), RegistryKey::getValue))
            .packetCodec(Identifier.PACKET_CODEC.xmap((id) -> RegistryKey.of(RegistryKeys.WORLD, id), RegistryKey::getValue)));
    public static DataComponentType<BlueprintPositionComponent> BLUEPRINT_POSITION = register("blueprint_position", (builder) -> builder
            .codec(BlueprintPositionComponent.CODEC)
            .packetCodec(BlueprintPositionComponent.PACKET_CODEC));
    public static DataComponentType<Long> BLUEPRINT_MINED_CHUNKS = register("blueprint_mined_chunks", (builder) -> builder
            .codec(Codec.LONG)
            .packetCodec(PacketCodecs.VAR_LONG));

    public static DataComponentType<RegistryKey<World>> QUARRY_MONITOR_WORLD = register("quarry_monitor_world", (builder) -> builder
            .codec(Identifier.CODEC.xmap((id) -> RegistryKey.of(RegistryKeys.WORLD, id), RegistryKey::getValue))
            .packetCodec(Identifier.PACKET_CODEC.xmap((id) -> RegistryKey.of(RegistryKeys.WORLD, id), RegistryKey::getValue)));
    public static DataComponentType<BlockPos> QUARRY_MONITOR_POSITION = register("quarry_monitor_position", (builder) -> builder
            .codec(BlockPos.CODEC)
            .packetCodec(BlockPos.PACKET_CODEC));
    public static DataComponentType<ItemStack> QUARRY_MONITOR_CACHED_QUARRY = register("quarry_monitor_cached_quarry", (builder) -> builder
            .codec(ItemStack.CODEC)
            .packetCodec(ItemStack.PACKET_CODEC));
    public static DataComponentType<Long> QUARRY_MONITOR_CACHED_TIMESTAMP = register("quarry_monitor_cached_timestamp", (builder) -> builder
            .codec(Codec.LONG)
            .packetCodec(PacketCodecs.VAR_LONG));


    /**
     * {@link net.minecraft.component.DataComponentTypes}
     */
    public static <T> DataComponentType<T> register(String id, UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
        return Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(Main.MOD_ID, id), (builderOperator.apply(DataComponentType.builder())).build());
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
