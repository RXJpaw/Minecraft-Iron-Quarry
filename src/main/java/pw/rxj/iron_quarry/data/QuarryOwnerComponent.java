package pw.rxj.iron_quarry.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;
import java.util.UUID;

public class QuarryOwnerComponent {
    private final UUID uuid;
    private final String name;

    private QuarryOwnerComponent(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }
    public static QuarryOwnerComponent of(UUID uuid, String name) {
        return new QuarryOwnerComponent(uuid, name);
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        return o instanceof QuarryOwnerComponent other && this.uuid.equals(other.uuid) && this.name.equals(other.name);
    }
    @Override
    public int hashCode() {
        return Objects.hash(this.uuid, this.name);
    }

    public UUID getUuid() {
        return this.uuid;
    }
    public String getName() {
        return this.name;
    }

    private static QuarryOwnerComponent read(RegistryFriendlyByteBuf buf) {
        return of(
                buf.readUUID(),
                buf.readUtf()
        );
    }
    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeUUID(this.getUuid());
        buf.writeUtf(this.getName());
    }

    public static final Codec<QuarryOwnerComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("uuid").forGetter(QuarryOwnerComponent::getUuid),
            Codec.STRING.fieldOf("name").forGetter(QuarryOwnerComponent::getName)
            ).apply(instance, QuarryOwnerComponent::of)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, QuarryOwnerComponent> PACKET_CODEC = StreamCodec.ofMember(QuarryOwnerComponent::write, QuarryOwnerComponent::read);
}
