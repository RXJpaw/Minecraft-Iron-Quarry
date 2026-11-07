package pw.rxj.iron_quarry.data;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AugmentCapacityUpgradesComponent {
    public static final AugmentCapacityUpgradesComponent EMPTY = new AugmentCapacityUpgradesComponent(Map.of());

    private final Map<Integer, Identifier> slots;

    private AugmentCapacityUpgradesComponent(Map<Integer, Identifier> slots) {
        this.slots = slots;
    }
    public static AugmentCapacityUpgradesComponent of(Map<Integer, Identifier> slots) {
        return new AugmentCapacityUpgradesComponent(Map.copyOf(slots));
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        return o instanceof AugmentCapacityUpgradesComponent other && this.slots.equals(other.slots);
    }
    @Override
    public int hashCode() {
        return this.slots.hashCode();
    }

    public boolean isFilled(int slot) {
        return this.slots.containsKey(slot);
    }
    public Optional<Item> getItem(int slot) {
        Identifier itemId = this.slots.get(slot);
        if(itemId == null) return Optional.empty();

        return BuiltInRegistries.ITEM.getOptional(itemId);
    }
    public int getFilledCount(int slotCount) {
        return (int) this.slots.keySet().stream().filter(slot -> slot < slotCount).count();
    }
    public AugmentCapacityUpgradesComponent with(int slot, Item item) {
        Map<Integer, Identifier> slots = new HashMap<>(this.slots);
        slots.put(slot, BuiltInRegistries.ITEM.getKey(item));

        return of(slots);
    }

    private static AugmentCapacityUpgradesComponent fromEntries(List<Pair<Integer, Identifier>> entries) {
        Map<Integer, Identifier> slots = new HashMap<>();
        entries.forEach(entry -> slots.put(entry.getFirst(), entry.getSecond()));

        return of(slots);
    }
    private List<Pair<Integer, Identifier>> toEntries() {
        return this.slots.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> Pair.of(entry.getKey(), entry.getValue()))
                .toList();
    }

    private static AugmentCapacityUpgradesComponent read(RegistryFriendlyByteBuf buf) {
        Map<Integer, Identifier> slots = new HashMap<>();
        int size = buf.readVarInt();

        for (int i = 0; i < size; i++) {
            slots.put(buf.readVarInt(), Identifier.STREAM_CODEC.decode(buf));
        }

        return of(slots);
    }
    private void write(RegistryFriendlyByteBuf buf) {
        List<Pair<Integer, Identifier>> entries = this.toEntries();
        buf.writeVarInt(entries.size());

        for (Pair<Integer, Identifier> entry : entries) {
            buf.writeVarInt(entry.getFirst());
            Identifier.STREAM_CODEC.encode(buf, entry.getSecond());
        }
    }

    private static final Codec<Pair<Integer, Identifier>> ENTRY_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("slot").forGetter(Pair::getFirst),
            Identifier.CODEC.fieldOf("item").forGetter(Pair::getSecond)
            ).apply(instance, Pair::of)
    );
    public static final Codec<AugmentCapacityUpgradesComponent> CODEC = ENTRY_CODEC.listOf().xmap(AugmentCapacityUpgradesComponent::fromEntries, AugmentCapacityUpgradesComponent::toEntries);
    public static final StreamCodec<RegistryFriendlyByteBuf, AugmentCapacityUpgradesComponent> PACKET_CODEC = StreamCodec.ofMember(AugmentCapacityUpgradesComponent::write, AugmentCapacityUpgradesComponent::read);
}
